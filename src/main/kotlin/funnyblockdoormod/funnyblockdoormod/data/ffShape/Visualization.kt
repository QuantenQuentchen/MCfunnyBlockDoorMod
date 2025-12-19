package funnyblockdoormod.funnyblockdoormod.data.ffShape

import net.minecraft.nbt.NbtCompound

class Visualization(
    var color: Int = 0xFF0000,
    var opacity: Float = 1.0f,
    var isVisible: Boolean = false
) {
    fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.putInt("color", this.color)
        tag.putFloat("opacity", this.opacity)
        tag.putBoolean("isVisible", this.isVisible)
        return tag
    }

    companion object {
        fun deserializeFromNbt(tag: NbtCompound): Visualization {
            val col = tag.getInt("color")
            val opacity = tag.getFloat("opacity")
            val isVisible = tag.getBoolean("isVisible")
            return Visualization(col, opacity, isVisible)
        }
    }
}