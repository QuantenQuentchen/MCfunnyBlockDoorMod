package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

class BlockPos3DGrid(private val sizeX: Int, private val sizeY: Int, val sizeZ: Int) {

    val bundleOffsets = mutableListOf<BlockBundle>()

    fun getBlock(x: Int, y: Int, z: Int): BlockBundle? {
        val idx = convertTo1D(x, y, z)
        val bundle = bundleOffsets[idx ?: return null]
        if(bundle.blockPos.x == 0 && bundle.blockPos.y == 0 && bundle.blockPos.z == 0) return null
        return bundle
    }

    fun getBlock(cords: Vec3i): BlockBundle? {
        return getBlock(cords.x, cords.y, cords.z)
    }

    fun setBlock(cords: Vec3d, pos: BlockPos) {
        bundleOffsets.add(BlockBundle(cords, pos))
    }

    fun buildGrid(){
        bundleOffsets.sortedWith(compareBy({ it.offset.x }, { it.offset.y }, { it.offset.z } ))
    }

    private fun convertTo1D(x: Int, y: Int, z: Int): Int? {

        //val idx = (z-1)*(y-1)*x+y+z

        //val idx = z + (sizeZ+1) * (x + (sizeX+1) * y)

        val idx = z * 25 + y * 5 + x

        //val idx = z * ((sizeY+1) * (sizeX+1)) + y * (sizeX+1) + x

        //val idx = z * (sizeY * sizeX) + y * sizeX + x
        return if (idx < bundleOffsets.size) idx else null
    }

}
