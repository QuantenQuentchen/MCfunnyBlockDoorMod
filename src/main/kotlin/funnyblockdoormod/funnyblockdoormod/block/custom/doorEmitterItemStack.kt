package funnyblockdoormod.funnyblockdoormod.block.custom

import net.minecraft.item.ItemStack

data class doorEmitterItemStack(val itemStack: ItemStack, val maxProgress: Int, var progress: Int = 0){

    private fun isFinished(): Boolean {
        return progress >= maxProgress
    }

    fun getFinishedItem(): ItemStack? {
        return if(isFinished()) itemStack else null
    }

    fun incrementProgress(amount: Int) {
        progress += amount
    }

}
