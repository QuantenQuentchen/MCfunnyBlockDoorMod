package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB.Companion.SIZE_U
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB.Companion.SIZE_V
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.ceil

class BlockPos3DGrid(private val sizeX: Int, private val sizeY: Int, private val sizeZ: Int) {

    val grid: Array<Array<Array<BlockBundle?>>> = Array(sizeX+1) { Array(sizeY+1) { arrayOfNulls(sizeZ+1) } }

    val testList = mutableListOf<BlockBundle>()

    val xSize = sizeX
    val ySize = sizeY
    val zSize = sizeZ

    fun getBlock(x: Int, y: Int, z: Int): BlockBundle? {
        return try {
            grid[x][y][z]
        } catch (e: Exception) {
            null
        }
    }

    fun sign(x: Double): Int {
        return when {
            x < 0 -> -1
            x > 0 -> 1
            else -> 0
        }
    }

    fun at(x: Int, y: Int, z: Int): BlockBundle? {
        return try {
            grid[x][y][z]
        } catch (e: Exception) {
            null
        }
    }

    fun setBlock(value: BlockBundle) {
        testList.add(value)
    }

    fun buildGrid(): List<BlockBundle> {
        return testList.sortedWith(compareBy({ it.offset.z }, { it.offset.x }, { it.offset.y } ))

    }

    fun setBlock(x: Int, y: Int, z: Int, value: BlockBundle?) {
        if(getBlock(x, y, z) != null){
            val bl = getBlock(x, y, z)
            val offset = bl?.offset
            val valOffset = value?.offset
            FunnyBlockDoorMod.logger.error("Detected collision at $x, $y, $z")
            if(value != null) {
                if (bl != null) {

                    FunnyBlockDoorMod.logger.error("Old block offset: ${offset!!.x}, ${offset.y}, ${offset.z}")
                    FunnyBlockDoorMod.logger.error("New block offset: ${valOffset!!.x}, ${valOffset.y}, ${valOffset.z}")
                    val xProbe = sign(valOffset.x - offset.x)
                    val yProbe = sign(valOffset.y - offset.y)
                    val zProbe = sign(valOffset.z - offset.z)

                    FunnyBlockDoorMod.logger.error("Probing at $xProbe, $yProbe, $zProbe")

                    if(at(x+xProbe, y, z) == null){
                        if(x+xProbe < grid.size && x+xProbe >= 0){
                            grid[x+xProbe][y][z] = value
                            FunnyBlockDoorMod.logger.error("Set block at ${x+xProbe}, $y, $z")
                        }
                    }
                    if(at(x, y+yProbe, z) == null){
                        if(y+yProbe < grid[0].size && y+yProbe >= 0) {
                            grid[x][y + yProbe][z] = value
                            FunnyBlockDoorMod.logger.error("Set block at $x, ${y+yProbe}, $z")
                        }
                    }
                    if(at(x, y, z+zProbe) == null){
                        if(z+zProbe < grid[0][0].size && z+zProbe >= 0){
                            grid[x][y][z+zProbe] = value
                            FunnyBlockDoorMod.logger.error("Set block at $x, $y, ${z+zProbe}")
                        }
                    }
                }
            }
        }

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

    fun iterator(): Iterator<BlockBundle?> {
        return object : Iterator<BlockBundle?> {

            override fun hasNext(): Boolean {
                return z < grid.size
            }

            override fun next(): BlockBundle? {
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

    fun reverseIterator(): Iterator<BlockBundle?> {
        return object : Iterator<BlockBundle?> {

            override fun hasNext(): Boolean {
                return z >= 0
            }

            override fun next(): BlockBundle? {
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

    fun getBlock1D(index: Int): BlockBundle? {
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
