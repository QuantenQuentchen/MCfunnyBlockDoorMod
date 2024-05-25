package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import com.mojang.datafixers.util.Pair
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier
import java.util.*
import kotlin.math.min

class DoorEmitterInventorySlot(inventory: doorEmitterInventory, private var index: Int, x: Int, y: Int)
    : Slot(inventory, index, x, y) {
        var depth: Int = 0

    fun modifyDepth(depth: Int) {
        this.depth = depth
    }

    private fun calculateIndex(): Int {
        //FunnyBlockDoorMod.logger.info("Index: $index, Depth: $depth")
        return index + (depth * 25)
    }

    override fun getStack(): ItemStack {
        return inventory.getStack(calculateIndex())
    }

    override fun canInsert(stack: ItemStack?): Boolean {
        return inventory.isValid(calculateIndex(), stack)
    }

    override fun canTakeItems(playerEntity: PlayerEntity): Boolean {
        return inventory.canPlayerUse(playerEntity)
    }

    fun isEmpty(): Boolean {
        return inventory.getStack(calculateIndex()).isEmpty
    }

    override fun onQuickTransfer(newItem: ItemStack, original: ItemStack) {
        val i = original.count - newItem.count
        if (i > 0) {
            this.onCrafted(original, i)
        }
    }

    override fun setStackNoCallbacks(stack: ItemStack?) {
        inventory.setStack(calculateIndex(), stack)
        this.markDirty()
    }

    override fun takeStack(amount: Int): ItemStack {
        return inventory.removeStack(calculateIndex(), amount)
    }

}