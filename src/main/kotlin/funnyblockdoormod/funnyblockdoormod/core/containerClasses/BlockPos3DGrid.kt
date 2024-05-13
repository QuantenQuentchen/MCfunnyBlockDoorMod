package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import net.minecraft.util.math.BlockPos

class BlockPos3DGrid(x: Int, y: Int, z: Int) {

    private val grid: Array<Array<Array<BlockPos?>>> = Array(x) { Array(y) { arrayOfNulls(z) } }

    fun getBlock(x: Int, y: Int, z: Int): BlockPos? {
        return grid[x][y][z]
    }

    fun setBlock(x: Int, y: Int, z: Int, value: BlockPos?) {
        grid[x][y][z] = value
    }

}
