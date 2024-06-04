package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB.Companion.SIZE_U
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB.Companion.SIZE_V
import net.minecraft.util.math.BlockPos

class BlockPos3DGrid(sizeX: Int, sizeY: Int, sizeZ: Int) {

    val grid: Array<Array<Array<BlockPos?>>> = Array(sizeX) { Array(sizeY) { arrayOfNulls(sizeZ) } }

    fun getBlock(x: Int, y: Int, z: Int): BlockPos? {
        return try {
            grid[x][y][z]
        } catch (e: Exception) {
            null
        }
    }

    fun setBlock(x: Int, y: Int, z: Int, value: BlockPos?) {
        try {
            grid[x][y][z] = value
        } catch (e: Exception) {
            FunnyBlockDoorMod.logger.error("Error setting block at $x, $y, $z in grid of size ${grid.size}, ${grid[0].size}, ${grid[0][0].size}")
        }
        //grid[x][y][z] = value
    }

    private val middle = ((grid[0].size + 1) / 2) -1

    private var x = middle
    private var y = middle
    private var z = 0
    private var state = 0
    private var layer = 0

    fun iterator(): Iterator<BlockPos?> {
        return object : Iterator<BlockPos?> {

            override fun hasNext(): Boolean {
                return z < grid.size
            }

            override fun next(): BlockPos? {
                val item = when (state) {
                    0 -> grid.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    1 -> grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
                    2 -> grid.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    3 -> grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    else -> null
                }
                state = (state + 1) % 4
                if (state == 0) {
                    layer++
                    if (layer > x || layer > y) {
                        layer = 0
                        if (z < grid.size - 1) {
                            z++
                        }
                    }
                }
                return item
            }
        }
    }

    fun reverseIterator(): Iterator<BlockPos?> {
        return object : Iterator<BlockPos?> {

            override fun hasNext(): Boolean {
                return z >= 0
            }

            override fun next(): BlockPos? {
                val item = when (state) {
                    0 -> grid.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    1 -> grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    2 -> grid.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    3 -> grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
                    else -> null
                }
                state = (state + 1) % 4
                if (state == 0) {
                    layer++
                    if (layer > x || layer > y) {
                        layer = 0
                        if (z > 0) {
                            z--
                        }
                    }
                }
                return item
            }
        }
    }

    fun getBlock1D(index: Int): BlockPos? {
        val totalElements = grid.size * grid[0].size * grid[0][0].size
        if (index >= totalElements) {
            return null
        }
        val z = index / (grid[0].size * grid[0][0].size)
        val y = (index - z * grid[0].size * grid[0][0].size) / grid[0].size
        val x = index - y * grid[0].size - z * grid[0].size * grid[0][0].size
        return getBlock(x, y, z)
    }

}
