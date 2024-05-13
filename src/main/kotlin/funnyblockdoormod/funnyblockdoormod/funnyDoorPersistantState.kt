package funnyblockdoormod.funnyblockdoormod

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager
import java.util.function.Function

class funnyDoorPersistantState: PersistentState() {

    var protectedDoorBlocks: MutableSet<BlockPos> = mutableSetOf()

    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        val list = NbtList()
        for (pos in protectedDoorBlocks) {
            val posList = NbtList()
            posList.add(NbtInt.of(pos.x))
            posList.add(NbtInt.of(pos.y))
            posList.add(NbtInt.of(pos.z))
            list.add(posList)
        }
        nbt?.put("protectedDoorBlocks", list)

        return nbt!!
    }

    fun getLength(): Int {
        return protectedDoorBlocks.size
    }

    fun addBlock(pos: BlockPos) {
        protectedDoorBlocks.add(pos)
        markDirty()
    }

    fun removeBlock(pos: BlockPos) {
        protectedDoorBlocks.remove(pos)
        markDirty()
    }

    companion object {

        fun createFromNbt(tag: NbtCompound): funnyDoorPersistantState {
            val state = funnyDoorPersistantState()
            val list = tag.getList("protectedDoorBlocks", 9)
            for (i in 0 until list.size) {
                val posList = list.getList(i)
                val pos = BlockPos(posList.getInt(0), posList.getInt(1), posList.getInt(2))
                state.protectedDoorBlocks.add(pos)
            }
            return state
        }
        fun getServerState(server: MinecraftServer): funnyDoorPersistantState {
            val persistentStateManager: PersistentStateManager = server.overworld.persistentStateManager
            val state: funnyDoorPersistantState = persistentStateManager.getOrCreate(::createFromNbt, { funnyDoorPersistantState() }, FunnyBlockDoorMod.MOD_ID)
            state.markDirty()
            return state
        }
    }
}
