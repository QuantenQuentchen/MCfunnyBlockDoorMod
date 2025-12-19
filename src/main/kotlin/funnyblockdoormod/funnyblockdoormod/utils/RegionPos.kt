package funnyblockdoormod.funnyblockdoormod.utils

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

data class RegionPos(val x: Int, val y: Int){
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        //THIS IS FUCKING STUPID, the fuck is an NbtInt ?
        nbt.putInt("x", x)
        nbt.putInt("y", y)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): RegionPos {
            return RegionPos(nbt.getInt("x"), nbt.getInt("y"))
        }
        private const val REGION_SIZE = 32 // chunks per region (32x32)

        /** Convert chunk coordinates to region coordinates (handles negatives) */
        fun fromChunk(chunkX: Int, chunkZ: Int): RegionPos {
            return RegionPos(Math.floorDiv(chunkX, REGION_SIZE), Math.floorDiv(chunkZ, REGION_SIZE))
        }
        fun fromChunk(chunkPos: ChunkPos): RegionPos {
            return RegionPos(Math.floorDiv(chunkPos.x, REGION_SIZE), Math.floorDiv(chunkPos.z, REGION_SIZE))
        }

        /** Convert block coordinates to region coordinates */
        fun fromBlock(blockX: Int, blockZ: Int): RegionPos {
            val chunkX = blockX shr 4 // 16 blocks per chunk
            val chunkZ = blockZ shr 4
            return RegionPos(Math.floorDiv(chunkX, REGION_SIZE), Math.floorDiv(chunkZ, REGION_SIZE))
        }
        fun fromBlock(blockPos: BlockPos): RegionPos {
            val chunkX = blockPos.x shr 4
            val chunkZ = blockPos.z shr 4
            return RegionPos(Math.floorDiv(chunkX, REGION_SIZE), Math.floorDiv(chunkZ, REGION_SIZE))
        }
    }
}
