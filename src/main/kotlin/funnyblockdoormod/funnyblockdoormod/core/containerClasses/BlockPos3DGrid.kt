package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import kotlin.math.abs

class BlockPos3DGrid(private val sizeX: Int, private val sizeY: Int, val sizeZ: Int) {

    val bundleOffsets = mutableListOf<BlockBundle>()

    private val sizeZcorr = sizeZ + 1

    private fun getBlock(x: Int, y: Int, z: Int): BlockBundle? {
        val idx = convertTo1D(x, y, z)
        val bundle = bundleOffsets[idx ?: return null]
        if(bundle.blockPos.x == 0 && bundle.blockPos.y == 0 && bundle.blockPos.z == 0) return null
        return bundle
    }

    private fun transform(cords: Vec3i): Vec3i{
        return Vec3i(
            abs(cords.x - (sizeX-1)),
            abs(cords.y - (sizeY-1)),
            cords.z
        )
    }

    fun getBlock(cords: Vec3i): BlockBundle? {
        val transCords = transform(cords)
        return getBlock(transCords.x, transCords.y, transCords.z)
    }

    fun setBlock(cords: Vec3d, pos: BlockPos) {
        bundleOffsets.add(BlockBundle(cords, pos))
    }

    fun buildGrid(){
        bundleOffsets.sortedWith(compareBy({ it.offset.x }, { it.offset.y }, { it.offset.z } ))
    }

    private fun convertTo1D(x: Int, y: Int, z: Int): Int? {
        val idx = (y + x * sizeY ) * sizeZcorr + z
        if(idx > bundleOffsets.size){ FunnyBlockDoorMod.logger.error("This shouldn't be happening: $idx $x, $y, $z") }
        return if (idx < bundleOffsets.size) idx else null
    }

}
