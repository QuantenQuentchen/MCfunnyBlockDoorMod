package funnyblockdoormod.funnyblockdoormod.screen

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterInventory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.doorEmitterBlockEntity
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.DoorEmitterInventorySlot
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier

class DoorEmitterScreenHandler: ScreenHandler, InventoryChangedListener{

    private val inventory: doorEmitterInventory
    private val propertyDelegate: PropertyDelegate
    private val blockEntity: doorEmitterBlockEntity

    private var xAngle: Int = 0
    private var yAngle: Int = 0
    private var zAngle: Int = 0

    private var blockedSlots: MutableSet<Int> = mutableSetOf()

    private val dynamicSlots: MutableList<DoorEmitterInventorySlot> = mutableListOf()

    private var invDepth: Int = 0

    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf):
            this(syncId, inventory, inventory.player.world.getBlockEntity(buf.readBlockPos()), ArrayPropertyDelegate(2))

    constructor(syncId: Int, playerInventory: PlayerInventory, blockEntity: BlockEntity?, propertyDelegate: PropertyDelegate)
            : super(ModScreenHandlers.DOOREMITTERSCREENHANDLER, syncId) {

        //checkSize(blockEntity as Inventory, 2)
        this.inventory = (blockEntity as doorEmitterBlockEntity).inventory
        this.inventory.addListener(this as InventoryChangedListener)
        playerInventory.onOpen(playerInventory.player)
        this.propertyDelegate = propertyDelegate
        this.blockEntity = blockEntity

        addMainSlots(inventory)
        //addUpgradeSlots(inventory)

        addPlayerHotbar(playerInventory, 0, 36)
        addPlayerInventory(playerInventory, 0, 36)


        addProperties(propertyDelegate)

    }

    private fun addGridSlots(inventory: doorEmitterInventory, startSlotIndex: Int, topLeftX: Int, topLeftY: Int) {
        val slotSize = 18
        for (i in 0 until 5) {
            for (j in 0 until 5) {
                val x = topLeftX + j * slotSize
                val y = topLeftY + i * slotSize
                val slot = DoorEmitterInventorySlot(inventory, startSlotIndex + j + i * 5, x, y)
                this.addSlot(slot)
                dynamicSlots.add(slot)
            }
        }
    }

    private fun updateDynamicSlots(){
        for(slot in dynamicSlots){
            FunnyBlockDoorMod.logger.info("Slot: ${slot.id}")
            slot.modifyDepth(invDepth)
        }
    }

    fun incrementInvDepth(){
        invDepth++
        if(invDepth > 25){
            invDepth = 0
        }
        updateDynamicSlots()
        sendDepthUpdatePacketToServer()

    }

    fun decrementInvDepth(){
        invDepth--
        if(invDepth < 0){
            invDepth = 25
        }
        updateDynamicSlots()
        sendDepthUpdatePacketToServer()

    }

    private fun sendDepthUpdatePacketToServer() {
        // Create a packet that contains the new depth value
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(invDepth)

        // Send the packet to the server
        ClientPlayNetworking.send(Identifier("funnyblockdoormod", "update_depth"), buf)
    }

    fun setDepth(depth: Int){
        if(depth < 0 || depth > 25){
            return
        }
        invDepth = depth
        updateDynamicSlots()
    }



    private fun addMainSlots(playerInventory: doorEmitterInventory){
        addGridSlots(playerInventory, 0, 44, 17)
    }

    private fun addVerticalSlots(inventory: Inventory, startSlotIndex: Int, topLeftX: Int, topLeftY: Int, length: Int) {
        val slotSize = 18
        for (i in 0 until length) {
            val y = topLeftY + i * slotSize
            this.addSlot(Slot(inventory, startSlotIndex + i, topLeftX, y))
        }
    }

    private fun addUpgradeSlots(playerInventory: Inventory){
        addVerticalSlots(playerInventory, 25, 152, 17, 5)
    }

    private fun addCamoSlot(){

    }

    override fun canInsertIntoSlot(slot: Slot?): Boolean {
        if(blockedSlots.contains(slot?.id)){
            return false
        }
        return super.canInsertIntoSlot(slot)
    }

    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        val slot = slots[slot]
        if(slot != null && slot.hasStack()){
            val originalStack = slot.stack
            newStack = originalStack.copy()
            if(slot.id < 2){
                if(!insertItem(originalStack, 2, 38, true)){
                    return ItemStack.EMPTY
                }
            } else {
                if(!insertItem(originalStack, 0, 2, false)){
                    return ItemStack.EMPTY
                }
            }
            if(originalStack.isEmpty){
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return newStack
    }

    override fun onClosed(player: PlayerEntity?) {
        super.onClosed(player)
        inventory.removeListener(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return inventory.canPlayerUse(player)
    }



    override fun onContentChanged(inventory: Inventory?) {
        super.onContentChanged(inventory)
        sendContentUpdates()
    }

    private fun addPlayerInventory(inventory: PlayerInventory, widthOffset: Int = 0, heightOffset: Int = 0) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, widthOffset + 8 + j * 18, heightOffset + 84 + i * 18))
            }
        }
    }

    private fun addPlayerHotbar(inventory: PlayerInventory, widthOffset: Int = 0, heightOffset: Int = 0) {
        for (i in 0..8) {
            this.addSlot(Slot(inventory, i, widthOffset + 8 + i * 18, heightOffset + 142))
        }
    }

    fun getXAngle(): Int {
        return xAngle
    }
    fun getYAngle(): Int {
        return yAngle
    }
    fun getZAngle(): Int {
        return zAngle
    }
    fun getInvDepth(): Int {
        return invDepth
    }

    fun getBlockedSlots(): MutableSet<Int> {
        return blockedSlots
    }

    override fun onInventoryChanged(sender: Inventory?) {
        sendContentUpdates()
    }

}