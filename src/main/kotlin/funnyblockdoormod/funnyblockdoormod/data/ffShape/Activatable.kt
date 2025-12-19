package funnyblockdoormod.funnyblockdoormod.data.ffShape

import net.minecraft.nbt.NbtCompound

class Activatable(
    var isActive: Boolean = true
){

    fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.putBoolean("isActive", this.isActive)
        return tag
    }

    companion object{
        fun deserializeFromNbt(tag: NbtCompound): Activatable {
            val isActive = tag.getBoolean("isActive")
            return Activatable(isActive)
        }
    }
}