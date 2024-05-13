package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import net.minecraft.block.BlockState

class BlockState3DGrid(x: Int, y: Int, z: Int) {

    private val grid: Array<Array<Array<BlockState?>>> = Array(x) { Array(y) { arrayOfNulls(z) } }

    fun getBlock(x: Int, y: Int, z: Int): BlockState? {
        return grid[x][y][z]
    }

    fun setBlock(x: Int, y: Int, z: Int, value: BlockState?) {
        grid[x][y][z] = value
    }

}