package funnyblockdoormod.funnyblockdoormod.block

import net.minecraft.nbt.NbtCompound

typealias DoorBlockDataGrid = Array<Array<Array<doorBlockData>>>


class doorBlockDataGrid {
    private val grid: DoorBlockDataGrid = Array(9) { Array(5) { Array(5) { doorBlockData() } } }

    operator fun get(x: Int, y: Int, z: Int): doorBlockData {
        return grid[x][y][z]
    }

    operator fun set(x: Int, y: Int, z: Int, value: doorBlockData) {
        grid[x][y][z] = value
    }

    fun toNbt(nbt: NbtCompound) {
        for (i in grid.indices) {
            for (j in grid[i].indices) {
                for (k in grid[i][j].indices) {
                    val blockDataNbt = NbtCompound()
                    TODO("Implement this!")
                    // Serialize each doorBlockData instance into an NbtCompound
                    // You'll need to implement this in the doorBlockData class
                    //grid[i][j][k].toNbt(blockDataNbt)
                    //nbt.put("blockData_$i.$j.$k.", blockDataNbt)
                }
            }
        }
    }

    fun fromNbt(nbt: NbtCompound) {
        for (i in grid.indices) {
            for (j in grid[i].indices) {
                for (k in grid[i][j].indices) {
                    val blockDataNbt = nbt.getCompound("blockData_$i.$j.$k.")
                    TODO("Implement this!")
                    // Deserialize each doorBlockData instance from an NbtCompound
                    // You'll need to implement this in the doorBlockData class
                    //grid[i][j][k] = doorBlockData.fromNbt(blockDataNbt)
                }
            }
        }
    }

}