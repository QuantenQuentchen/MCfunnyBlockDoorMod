package funnyblockdoormod.funnyblockdoormod.serialize

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.MOD_ID
import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.logger
import funnyblockdoormod.funnyblockdoormod.data.ServerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.nbt.*
import net.minecraft.util.WorldSavePath
import java.io.*
import java.lang.ref.WeakReference
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.io.path.absolutePathString
import kotlin.to

abstract class Serializable {

    companion object {

        enum class RootRef{
            HARD,
            WEAK
        }

        //HEADER proposition 1:
        //[MAGIC][VERSION][KEY-SIZE][KEY-LIST][NBT-ROOT]

        //HEADER proposition 2:
        //[MAGIC][VERSION][KEY-SIZE][COUNTED][COUNTER][KEY-LIST][FREE-LIST][NBT-ROOT]

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private const val MAGIC = "FNBT"
        private const val VERSION = 1
        private val dirtyObjectsByRegion: ConcurrentHashMap<String, ConcurrentLinkedQueue<Serializable>> = ConcurrentHashMap()

        private val objectsHardRef: ConcurrentHashMap<String, ConcurrentHashMap<String, Serializable>> = ConcurrentHashMap()
        private val objectsWeakRef: ConcurrentHashMap<String, ConcurrentHashMap<String, WeakReference<Serializable>>> = ConcurrentHashMap()

        private val regionLocks: ConcurrentHashMap<String, ReentrantReadWriteLock> = ConcurrentHashMap()

        private val saveSemaphore: Semaphore = Semaphore(8)

        private fun getLock(region: String): ReentrantReadWriteLock =
            regionLocks.computeIfAbsent(region){ ReentrantReadWriteLock() }

        fun deleteHardRef(region: String, key: String){
            objectsHardRef.get(region)?.remove(key)
        }

        fun getCached(region: String, key: String): Serializable? {
            val hard = objectsHardRef[region]?.get(key)
            if (hard != null) return hard
            val wmap = objectsWeakRef[region] ?: return null
            val ref = wmap[key]
            val obj = ref?.get()
            if (obj == null) wmap.remove(key, ref)
            return obj
        }


        suspend fun get(
            refKind: RootRef, region: String, key: String,
            factory: (tag: NbtElement, region: String, key: String) -> Serializable
        ): Serializable? {
            val obj = getObject(region, key, factory) ?: return null
            return withRef(refKind, region,
                hardRefAction = { map -> map[key] = obj; obj },
                weakRefAction = { map -> map[key] = WeakReference(obj); obj}
            )
        }

        suspend fun getOrCreate(
            refKind: RootRef, region: String, key: String,
            factory: (tag: NbtElement, region: String, key: String) -> Serializable,
            supplier: () -> Serializable
        ): Serializable{
            val obj = getObject(region, key, factory)?: supplier().also {it.markDirty()}
            return withRef(refKind, region,
                hardRefAction = { map -> map[key] = obj; obj },
                weakRefAction = { map -> map[key] = WeakReference(obj); obj}
            )
        }

        internal inline fun <R> withRef(
            refKind: RootRef,
            region: String,
            crossinline hardRefAction: (MutableMap<String, Serializable>) -> R,
            crossinline weakRefAction: (MutableMap<String, WeakReference<Serializable>>) -> R
        ): R {
            return when (refKind){
                RootRef.HARD -> hardRefAction(objectsHardRef.computeIfAbsent(region) { ConcurrentHashMap()})
                RootRef.WEAK -> weakRefAction(objectsWeakRef.computeIfAbsent(region) { ConcurrentHashMap()})
            }
        }

        internal suspend inline fun getObject(
            region: String, key: String,
            crossinline factory: (tag: NbtElement, region: String, key: String) -> Serializable
        ): Serializable? = withContext(Dispatchers.IO) {
            val cached = getCached(region, key)
            if (cached != null) return@withContext cached
            returnTagIfKeyPresent(region, key)?.let { tag ->
                tag.get(key)?.let { value ->
                    return@withContext factory(value, region, key)
                }
                return@withContext null
            }
            return@withContext null
        }

        private suspend fun returnTagIfKeyPresent(region: String, key: String): NbtCompound? =
            withContext(Dispatchers.IO) {
                val path = getFilePath(region) ?: return@withContext null
                val lock = getLock(region)
                lock.readLock().lock()
                try {
                    Files.createDirectories(path.parent)
                    if (!Files.exists(path)) return@withContext null
                    DataInputStream(BufferedInputStream(Files.newInputStream(path))).use { stream ->
                        val magic = stream.readUTF()
                        val version = stream.readInt()
                        if (magic != MAGIC || version != VERSION) {
                            return@withContext null
                        }
                        val keyTag = NbtIo.read(stream)
                        val keyList = keyTag.getList("keys", NbtElement.STRING_TYPE.toInt())
                        for (k in keyList) if (k.asString() == key) return@withContext NbtIo.readCompressed(stream)
                        return@withContext null
                    }
                }
                catch (e: Exception){
                    logger.error("Failed to read file $path", e)
                }
                finally {
                    lock.readLock().unlock()
                }
                return@withContext null
            }

        private fun getFilePath(seri: Serializable): Path? = ServerHolder.server?.getSavePath(WorldSavePath.ROOT)
            ?.resolve("data")
            ?.resolve(MOD_ID)
            ?.resolve("${seri.getRegion()}.dat")

        private fun getFilePath(region: String): Path? = ServerHolder.server?.getSavePath(WorldSavePath.ROOT)
            ?.resolve("data")
            ?.resolve(MOD_ID)
            ?.resolve("$region.dat")

        private fun getTempFile(path: Path): Path = path.resolveSibling("${path.fileName}.tmp")

        private enum class NBTOperation{
            ADD,
            MODIFY,
            REMOVE
        }

        private fun modifyNBT(fileTag: NbtCompound, seri: Serializable): NBTOperation{
            if (seri.isDeleted){
                fileTag.remove(seri.getKey())
                return NBTOperation.REMOVE
            }
            val modified = fileTag.contains(seri.getKey())
            fileTag.put(seri.getKey(), seri.writeNBT())
            return if (modified) NBTOperation.MODIFY else NBTOperation.ADD
        }

        private fun fileOp(region: String, queue: ConcurrentLinkedQueue<Serializable>){
            scope.launch {
                val lock = getLock(region)
                lock.writeLock().lock()
                saveSemaphore.acquire()
                try {
                    val (fileTag, keyset) = readFile(region)
                    while (true) {
                        val obj = queue.poll() ?: break
                        val op = modifyNBT(fileTag, obj)
                        if (op == NBTOperation.ADD) {
                            keyset.add(obj.getKey())
                        } else if (op == NBTOperation.REMOVE) {
                            keyset.remove(obj.getKey())
                        }
                    }
                    try {
                        writeFile(region, fileTag, keyset)
                    } catch (e: Exception) {
                        // put remaining items back into global dirty queue
                        val fallback = dirtyObjectsByRegion.computeIfAbsent(region) { ConcurrentLinkedQueue() }
                        while (true) {
                            val obj = queue.poll() ?: break
                            fallback.add(obj)
                        }
                    }
                } finally {
                    lock.writeLock().unlock()
                    saveSemaphore.release()
                }
            }
        }

        private suspend fun readFile(region: String): Pair<NbtCompound, HashSet<String>>
        = withContext(Dispatchers.IO) {
            val path = getFilePath(region) ?: return@withContext NbtCompound() to HashSet()
            Files.createDirectories(Path.of(path.absolutePathString()).parent)
            if (!Files.exists(path)) return@withContext NbtCompound() to HashSet()
            DataInputStream(BufferedInputStream(Files.newInputStream(path))).use { stream ->
                val magic = stream.readUTF()
                val version = stream.readInt()
                if (magic != MAGIC || version != VERSION) {
                    return@withContext NbtCompound() to HashSet()
                }
                val keyTag = NbtIo.read(stream)
                val keyList = keyTag.getList("keys", NbtElement.STRING_TYPE.toInt())
                val keyset = HashSet<String>()
                for (k in keyList) keyset.add(k.asString())
                return@withContext NbtIo.readCompressed(stream) to keyset
            }
        }

        private suspend fun writeFile(region: String, fileNbt: NbtCompound, keyset: HashSet<String>)
        = withContext(Dispatchers.IO) {
            val path = getFilePath(region) ?: return@withContext
            val tmpPath = getTempFile(path).absolutePathString()
            Files.createDirectories(Path.of(tmpPath).parent)
            DataOutputStream(BufferedOutputStream(FileOutputStream(tmpPath))).use { stream ->
                stream.writeUTF(MAGIC)
                stream.writeInt(VERSION)

                val keyTag = NbtCompound()
                val keyList = NbtList()
                for (k in keyset) keyList.add(NbtString.of(k))
                keyTag.put("keys", keyList)
                NbtIo.write(keyTag, stream)

                NbtIo.writeCompressed(fileNbt, stream)

            }
            tmpPath.let {

                try {
                    Files.move(Path.of(it), path, StandardCopyOption.ATOMIC_MOVE)
                } catch (ex: AtomicMoveNotSupportedException) {
                    Files.move(Path.of(it), path, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }


        fun saveState(){
            //TODO: Look into optimization here
            val regions = dirtyObjectsByRegion.keys.toList()
            for (region in regions) {
                val queue = dirtyObjectsByRegion.remove(region) ?: continue
                fileOp(region, queue)
            }
        }
        fun clearCaches() {
            for ((key, value) in objectsHardRef) {
                value.clear()
            }
            for ((key, value) in objectsWeakRef) {
                value.clear()
            }
            objectsHardRef.clear()
            objectsWeakRef.clear()
        }
    }

    private var isDeleted: Boolean = false
    //val size: Int = 4

    fun deleteSelf() {
        deleteHardRef(this.getRegion(), this.getKey())
        isDeleted = true
        markDirty()
    }

    protected fun markDirty() {
        dirtyObjectsByRegion.computeIfAbsent(this.getRegion()){ ConcurrentLinkedQueue()}.add(this)
    }

    abstract fun writeNBT(): NbtElement

    abstract fun getKey(): String

    abstract fun getRegion(): String

}