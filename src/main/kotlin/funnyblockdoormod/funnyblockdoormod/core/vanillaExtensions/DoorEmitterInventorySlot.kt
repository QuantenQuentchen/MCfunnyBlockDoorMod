package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterInventory
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import kotlin.math.min

class DoorEmitterInventorySlot(inventory: doorEmitterInventory, private var index: Int, x: Int, y: Int)
    : Slot(inventory, index, x, y) {
        var depth: Int = 0

    private fun calculateIndex(): Int {
        //FunnyBlockDoorMod.logger.info("Index: $index, Depth: $depth")
        //val depth = (inventory as doorEmitterInventory).depth
        return index// + (depth * 25)
    }

    private fun isBlock(itemStack: ItemStack): Boolean {
        val block = Block.getBlockFromItem(itemStack.item)
        return block != Blocks.AIR
    }

    override fun getStack(): ItemStack {
        return inventory.getStack(calculateIndex())
    }

    override fun canInsert(stack: ItemStack?): Boolean {
        return stack?.let { inventory.isValid(calculateIndex(), it) && isBlock(it) } ?: false
    }

    override fun canTakeItems(playerEntity: PlayerEntity): Boolean {
        return inventory.canPlayerUse(playerEntity)
    }

    fun isEmpty(): Boolean {
        return inventory.getStack(calculateIndex()).isEmpty
    }

    override fun onQuickTransfer(newItem: ItemStack, original: ItemStack) {
        FunnyBlockDoorMod.logger.info("Quick transfer")
/*        val i = original.count - newItem.count
        if (i > 0) {
            this.onCrafted(original, i)
        }*/
    }

    override fun setStackNoCallbacks(stack: ItemStack?) {
        inventory.setStack(calculateIndex(), stack)
        this.markDirty()
    }

    override fun takeStack(amount: Int): ItemStack {
        inventory.removeStack(calculateIndex(), amount)
        return ItemStack.EMPTY
    }

    override fun insertStack(stack: ItemStack): ItemStack {
        if (this.canInsert(stack)) {
            // Set the slot's stack to a single item from the input stack, without decreasing the input stack
            this.setStackNoCallbacks(stack.copy().apply { count = 1 })
        }
        return stack
    }

    override fun insertStack(stack: ItemStack?, count: Int): ItemStack {

        if(stack == null) return ItemStack.EMPTY

        val copStack = stack.copy()
        copStack.count = 1
        this.stack = copStack
        return stack
    }

    override fun setStack(stack: ItemStack?) {
        if (stack != null) {
            inventory.setStack(calculateIndex(), stack.copyWithCount(1))
        }
    }
}