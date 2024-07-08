package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class singleItemStack(item: Item) {

    private val internalStack: ItemStack = ItemStack(item, 1)

    fun getStack(): ItemStack {
        return internalStack
    }

    fun setCount(count: Int) {
        internalStack.count = count
    }

}