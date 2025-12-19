package funnyblockdoormod.funnyblockdoormod.screenhandler

import funnyblockdoormod.funnyblockdoormod.block.entitiy.ForceFieldBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class ForceFieldScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    blockEntity: BlockEntity?
) : ScreenHandler(ModScreenHandlers.FORCE_FIELD_SCREEN_HANDLER, syncId) {

    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf):
            this(syncId, inventory, inventory.player.world.getBlockEntity(buf.readBlockPos()))//, ArrayPropertyDelegate(3))

    private var blockEntity: ForceFieldBlockEntity? = blockEntity as ForceFieldBlockEntity?

    init {

        // Add slots, property delegates, etc.
        //checkSize(blockEntity, 1) // or however many slots

        // Add player inventory slots
        for (i in 0..2) {
            for (j in 0..8) {
                addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        // Hotbar
        for (i in 0..8) {
            addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
        }
    }

    override fun quickMove(
        player: PlayerEntity?,
        slot: Int
    ): ItemStack? {
        TODO("Not yet implemented")
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return true
        //TODO("Not yet implemented")
    }
}