package funnyblockdoormod.funnyblockdoormod.data.ffShape

import net.minecraft.nbt.NbtCompound

class Collapsible(
    val threshold: Double
) {

    fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("Threshold", this.threshold)
        return tag
    }
    companion object {
        fun deserializeFromNbt(tag: NbtCompound): Collapsible {
            val threshold = tag.getDouble("Threshold")
            return Collapsible(threshold)
        }
    }
}