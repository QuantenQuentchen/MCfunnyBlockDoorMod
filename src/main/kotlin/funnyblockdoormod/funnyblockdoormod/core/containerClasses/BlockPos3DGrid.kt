package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class BlockPos3DGrid(private val sizeX: Int, private val sizeY: Int, private val sizeZ: Int) {

    //val grid: Array<Array<Array<BlockBundle?>>> = Array(sizeX) { Array(sizeY) { arrayOfNulls(sizeZ) } }

    private val bundleOffsets = mutableListOf<BlockBundle>()

    fun getBlock(x: Int, y: Int, z: Int): BlockBundle? {
        val idx = convertTo1D(x, y, z)
        return if (idx != null) bundleOffsets[idx] else null
    }

    fun setBlock(cords: Vec3d, pos: BlockPos) {
        bundleOffsets.add(BlockBundle(cords, pos))
    }

    fun buildGrid(){
        bundleOffsets.sortedWith(compareBy({ it.offset.z }, { it.offset.y }, { it.offset.x } )) //probably switch x and y
    }

    private val middle = ((sizeY + 1) / 2) -1

    private var x = middle
    private var y = middle
    private var z = 0
    private var state = 0
    private var layer = 0

    fun iterator(): Iterator<BlockBundle?> {
        return object : Iterator<BlockBundle?> {

            override fun hasNext(): Boolean {
                return z < sizeZ
            }

            override fun next(): BlockBundle? {
                val item = when (state) {
                    0 -> getBlock(x, y-layer, z)//grid.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    1 -> getBlock(x+layer, y, z)//grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
                    2 -> getBlock(x, y+layer, z)//grid.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    3 -> getBlock(x-layer, y, z)//grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    else -> null
                }
                state = (state + 1) % 4
                if (state == 0) {
                    layer++
                    if (layer > x || layer > y) {
                        layer = 0
                        if (z < sizeZ - 1) {
                            z++
                        }
                    }
                }
                return item
            }
        }
    }

    fun reverseIterator(): Iterator<BlockBundle?> {
        return object : Iterator<BlockBundle?> {

            override fun hasNext(): Boolean {
                return z >= 0
            }

            override fun next(): BlockBundle? {
                val item = when (state) {
                    0 -> getBlock(x, y+layer, z)//grid.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    1 -> getBlock(x-layer, y, z)//grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    2 -> getBlock(x, y-layer, z)//grid.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    3 -> getBlock(x+layer, y, z)//grid.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
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

    private fun convertTo1D(x: Int, y: Int, z: Int): Int? {
        val idx = x + y * sizeZ + z * sizeZ * sizeY
        return if (idx < bundleOffsets.size) idx else null
    }

}
