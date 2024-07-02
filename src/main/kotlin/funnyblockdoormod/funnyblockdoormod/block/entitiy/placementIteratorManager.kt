package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3i

class placementIteratorManager() {

    companion object {
        fun fromNbt(nbt: NbtCompound?): placementIteratorManager {
            val manager = placementIteratorManager()

            if (nbt == null) return manager

            manager.fromNbt(nbt)
            return manager
        }
    }

    private val maxX = 5
    private val maxY = 5
    private val maxZ = 25

    private var x = getMiddle(maxX)
    private var y = getMiddle(maxY)
    private var z = 0
    private var state = 0
    private var layer = 0


    private fun getMiddle(size: Int): Int {
        return (size + 1) / 2
    }

    //private val middle = ((sizeY + 1) / 2) -1

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putInt("x", x)
        nbt.putInt("y", y)
        nbt.putInt("z", z)
        nbt.putInt("state", state)
        nbt.putInt("layer", layer)
        return nbt
    }

    fun fromNbt(nbt: NbtCompound) {
        x = nbt.getInt("x")
        y = nbt.getInt("y")
        z = nbt.getInt("z")
        state = nbt.getInt("state")
        layer = nbt.getInt("layer")
    }


    fun hasNext(): Boolean {
        return z < maxZ
    }

    fun next(): Vec3i? {

        FunnyBlockDoorMod.logger.info("x: $x, y: $y, z: $z, state: $state, layer: $layer")

        val item = when (state) {
            0 -> Vec3i(z, y - layer, x) // Up
            1 -> Vec3i(z, y, x + layer) // Right
            2 -> Vec3i(z, y + layer, x) // Down
            3 -> Vec3i(z, y, x - layer) // Left
            else -> null
        }
        state = (state + 1) % 4
        if (state == 0) {
            layer++
            if (layer > x || layer > y) {
                layer = 0
                if (z < maxZ - 1) {
                    z++
                }
            }
        }
        return item
    }


    fun revHasNext(): Boolean {
        return z >= 0
    }

    fun revNext(): Vec3i? {

        FunnyBlockDoorMod.logger.info("Reverse: x: $x, y: $y, z: $z, state: $state, layer: $layer")

        val item = when (state) {
            0 -> Vec3i(z, y + layer, x) // Down
            1 -> Vec3i(z, y, x - layer) // Left
            2 -> Vec3i(z, y - layer, x) // Up
            3 -> Vec3i(z, y, x + layer) // Right
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