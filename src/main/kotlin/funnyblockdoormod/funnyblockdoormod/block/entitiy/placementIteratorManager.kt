package funnyblockdoormod.funnyblockdoormod.block.entitiy

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

    private val midX = getMiddle(maxX)
    private val midY = getMiddle(maxY)

    private var idx = 0

    private val state: Int
        get() = (idx) % 25

    private val layer: Int
        get() = (idx).floorDiv(25)

    private fun getPos(): Vec3i {

        val layerVec = Vec3i(midX, midY, layer)

        val direction = determineDirection()

        return direction.add(layerVec)
    }


    private fun incrementIdx() {
        idx++
    }

    private fun decrementIdx() {
        idx--
    }

    private fun determineDirection(): Vec3i {
        return when (state) {
            0 -> Vec3i(0, 0, 0)
            1 -> Vec3i(0, 1, 0)
            2 -> Vec3i(1, 0, 0)
            3 -> Vec3i(0, -1, 0)
            4 -> Vec3i(-1, 0, 0)

            5 -> Vec3i(-1, -1, 0)
            6 -> Vec3i(-1, 1, 0)
            7 -> Vec3i(1, -1, 0)
            8 -> Vec3i(1, 1, 0)

            9 -> Vec3i(0, 2, 0)
            10 -> Vec3i(2, 0, 0)
            11 -> Vec3i(0, -2, 0)
            12 -> Vec3i(-2, 0, 0)

            13 -> Vec3i(-2, -2, 0)
            14 -> Vec3i(-2, 2, 0)
            15 -> Vec3i(2, -2, 0)
            16 -> Vec3i(2, 2, 0)

            17 -> Vec3i(-2, -1, 0)
            18 -> Vec3i(-2, 1, 0)
            19 -> Vec3i(2, -1, 0)
            20 -> Vec3i(2, 1, 0)

            21 -> Vec3i(-1, -2, 0)
            22 -> Vec3i(-1, 2, 0)
            23 -> Vec3i(1, -2, 0)
            24 -> Vec3i(1, 2, 0)

            else -> Vec3i(0, 0, 0)
        }
    }

    private fun getMiddle(size: Int): Int {
        return size.floorDiv(2)
    }

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putInt("idx", idx)
        return nbt
    }

    fun fromNbt(nbt: NbtCompound) {
        idx = nbt.getInt("idx")
    }


    fun hasNext(): Boolean {
        return idx < maxX * maxY * maxZ
    }

    fun next(): Vec3i {
        val pos = getPos()
        incrementIdx()
        return pos
    }


    fun hasPrevious(): Boolean {
        return idx >= 0
    }

    fun previous(): Vec3i {
        val pos = getPos()
        decrementIdx()
        return pos
    }

}