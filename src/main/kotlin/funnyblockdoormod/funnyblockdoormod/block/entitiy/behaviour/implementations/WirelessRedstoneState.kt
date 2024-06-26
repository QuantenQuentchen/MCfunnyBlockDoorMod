package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockPosDim
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.World

class WirelessRedstoneState(nbt: NbtCompound?) : PersistentState() {
    private var stupidRegistry: MutableMap<RegistryKey<World>, Int> = mutableMapOf()
    var blockChannels: MutableMap<Int, MutableList<BlockPosDim>> = mutableMapOf()

    private var veryStupidStateTracker: MutableMap<Int, MutableSet<BlockPosDim>> = mutableMapOf() //Left multi Dimensional for eventual transmitter tracker

    private val worldRegistryIdentifier = Identifier("minecraft", "dimension")

    init {
        if (nbt != null) {

            // Deserialize veryStupidStateTracker
            val veryStupidStateTrackerNbt = nbt.getCompound("veryStupidStateTracker")
            for (key in veryStupidStateTrackerNbt.keys) {
                val listNbt = veryStupidStateTrackerNbt.getList(key, 10) // 99 is the NBT type for LongTag
                val blockPosSet = mutableSetOf<BlockPosDim>()
                for (i in 0 until listNbt.size) {
                    val blockPosDimNbt = listNbt.getCompound(i)
                    val pos = BlockPos.fromLong(blockPosDimNbt.getLong("pos"))
                    val dim = blockPosDimNbt.getInt("dim")
                    blockPosSet.add(BlockPosDim(pos, dim))
                }
                veryStupidStateTracker[key.toInt()] = blockPosSet
            }

            // Deserialize stupidRegistry
            val stupidRegistryNbt = nbt.getCompound("stupidRegistry")
            val worldRegistry = RegistryKey.ofRegistry<World>(worldRegistryIdentifier)
            for (key in stupidRegistryNbt.keys) {
                val registryKey: RegistryKey<World> = RegistryKey.of(worldRegistry, Identifier.tryParse(key) ?: continue)
                stupidRegistry[registryKey] = stupidRegistryNbt.getInt(key)
            }

            // Deserialize blockChannels
            val blockChannelsNbt = nbt.getCompound("blockChannels")
            for (key in blockChannelsNbt.keys) {
                val listNbt = blockChannelsNbt.getList(key, 10) // 10 is the NBT type for CompoundTag
                val blockPosDimList = mutableListOf<BlockPosDim>()
                for (i in 0 until listNbt.size) {
                    val blockPosDimNbt = listNbt.getCompound(i)
                    val pos = BlockPos.fromLong(blockPosDimNbt.getLong("pos"))
                    val dim = blockPosDimNbt.getInt("dim")
                    blockPosDimList.add(BlockPosDim(pos, dim))
                }
                blockChannels[key.toInt()] = blockPosDimList
            }
        }
    }
    fun register(key: RegistryKey<World>): Int {
        return stupidRegistry.getOrPut(key) {
            stupidRegistry.size
        }
    }

    fun unregister(key: RegistryKey<World>) {
        stupidRegistry.remove(key)
    }

    fun getId(key: RegistryKey<World>): Int? {
        return stupidRegistry[key]
    }

    fun getKey(id: Int): RegistryKey<World>? {
        return stupidRegistry.entries.find { it.value == id }?.key
    }

    fun addTransmitter(channel: Int, pos: BlockPosDim) {
        veryStupidStateTracker.getOrPut(channel) { mutableSetOf() }.add(pos)
    }

    fun removeTransmitter(channel: Int, pos: BlockPosDim) {
        veryStupidStateTracker[channel]?.remove(pos)
    }

    fun getTransmitters(channel: Int): Set<BlockPosDim> {
        return veryStupidStateTracker[channel] ?: emptySet()
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {

        val veryStupidStateTrackerNbt = NbtCompound()
        for ((key, value) in veryStupidStateTracker) {
            val listNbt = NbtList()
            for (blockPos in value) {
                val blockPosDimNbt = NbtCompound()
                blockPosDimNbt.putLong("pos", blockPos.pos.asLong())
                blockPosDimNbt.putInt("dim", blockPos.dim)
                listNbt.add(blockPosDimNbt)
            }
            veryStupidStateTrackerNbt.put(key.toString(), listNbt)
        }
        nbt.put("veryStupidStateTracker", veryStupidStateTrackerNbt)

        // Serialize stupidRegistry
        val stupidRegistryNbt = NbtCompound()
        for ((key, value) in stupidRegistry) {
            stupidRegistryNbt.putInt(key.value.toString(), value)
            FunnyBlockDoorMod.logger.error("key: ${key}, value: $value")
        }
        nbt.put("stupidRegistry", stupidRegistryNbt)

        // Serialize blockChannels
        val blockChannelsNbt = NbtCompound()
        for ((key, value) in blockChannels) {
            val listNbt = NbtList()
            for (blockPosDim in value) {
                val blockPosDimNbt = NbtCompound()
                blockPosDimNbt.putLong("pos", blockPosDim.pos.asLong())
                blockPosDimNbt.putInt("dim", blockPosDim.dim)
                listNbt.add(blockPosDimNbt)
            }
            blockChannelsNbt.put(key.toString(), listNbt)
        }
        nbt.put("blockChannels", blockChannelsNbt)

        return nbt
    }
}