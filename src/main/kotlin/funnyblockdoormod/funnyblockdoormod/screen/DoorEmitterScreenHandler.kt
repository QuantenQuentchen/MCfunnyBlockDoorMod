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
    private var lastBlockedSlotCompund: Int = 0

    private val dynamicSlots: MutableList<DoorEmitterInventorySlot> = mutableListOf()

    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf):
            this(syncId, inventory, inventory.player.world.getBlockEntity(buf.readBlockPos()), ArrayPropertyDelegate(3))

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

    fun setChannel(channel: Int) {
        blockEntity.setNewChannel(channel)
        /*val currentChannels = propertyDelegate.get(1)
        val newChannels = if(isActive){
            currentChannels or (1 shl channel)
        } else {
            currentChannels and (1 shl channel).inv()
        }
        propertyDelegate.set(1, newChannels)*/
    }

    fun incrementInvDepth(){
        sendDepthDeltaPacketToServer(1)
    }

    fun decrementInvDepth(){
        sendDepthDeltaPacketToServer(-1)

    }

    private fun sendDepthDeltaPacketToServer(invDepthDelta: Int) {
        // Create a packet that contains the new depth value
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBlockPos(blockEntity.pos)
        buf.writeInt(invDepthDelta)

        // Send the packet to the server
        ClientPlayNetworking.send(Identifier(FunnyBlockDoorMod.MOD_ID, "update_depth_d"), buf)
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
        getBlockedSlotsDelegate()
        if(blockedSlots.contains(slot?.id)){
            return false
        }
        return super.canInsertIntoSlot(slot)
    }

    fun getEnergyStorage(): Int {
        return propertyDelegate.get(2)
    }

    override fun endQuickCraft() {
        super.endQuickCraft()
    }

    override fun quickMove(player: PlayerEntity?, slotId: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        val slot = slots[slotId]
        if(slot.hasStack()){
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

    private fun decodeBlockedStates(compundInt: Int){
        blockedSlots.clear()
        for (i in 0 .. 24){
            if(compundInt and (1 shl i) != 0){
                blockedSlots.add(i)
            }
        }
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
        return propertyDelegate.get(0)
    }

    private fun getBlockedSlotsDelegate() {
        val compundInt = this.propertyDelegate.get(1)
        if(lastBlockedSlotCompund == compundInt) return
        lastBlockedSlotCompund = compundInt
        decodeBlockedStates(this.propertyDelegate.get(1))
        return
    }

    fun getBlockedSlots(): MutableSet<Int> {
        getBlockedSlotsDelegate()
        return blockedSlots
    }

    fun incrementXAngle(){
        xAngle++
        if(xAngle > 360){
            xAngle = 0
        }
        //sendAngleUpdatePacketToServer()
    }

    fun decrementXAngle(){
        xAngle--
        if(xAngle < 0){
            xAngle = 360
        }
        //sendAngleUpdatePacketToServer()
    }

    fun incrementYAngle(){
        yAngle++
        if(yAngle > 360){
            yAngle = 0
        }
        //sendAngleUpdatePacketToServer()
    }

    fun decrementYAngle(){
        yAngle--
        if(yAngle < 0){
            yAngle = 360
        }
        //sendAngleUpdatePacketToServer()
    }

    fun incrementZAngle(){
        zAngle++
        if(zAngle > 360){
            zAngle = 0
        }
        //sendAngleUpdatePacketToServer()
    }

    fun decrementZAngle(){
        zAngle--
        if(zAngle < 0){
            zAngle = 360
        }
        //sendAngleUpdatePacketToServer()
    }

    override fun onInventoryChanged(sender: Inventory?) {
        //FunnyBlockDoorMod.logger.info("Inventory change detected for door emitter block entity")
        sendContentUpdates()
    }


    override fun insertItem(stack: ItemStack?, startIndex: Int, endIndex: Int, fromLast: Boolean): Boolean {
        var bl = false
        var i = startIndex
        if (fromLast) {
            i = endIndex - 1
        }
        var slot: Slot
        var itemStack: ItemStack
        if (stack!!.isStackable) {
            while (!stack.isEmpty) {
                if (fromLast) {
                    if (i < startIndex) {
                        break
                    }
                } else if (i >= endIndex) {
                    break
                }

                slot = slots[i]
                itemStack = slot.stack
                if (!itemStack.isEmpty && ItemStack.canCombine(stack, itemStack)) {
                    val j = itemStack.count + stack.count
                    if (j <= stack.maxCount) {
                        //stack.count = 0
                        itemStack.count = 1
                        slot.markDirty()
                        bl = true
                    } else if (itemStack.count < stack.maxCount) {
                        //stack.decrement(stack.maxCount - itemStack.count)
                        itemStack.count = 1
                        slot.markDirty()
                        bl = true
                    }
                }

                if (fromLast) {
                    --i
                } else {
                    ++i
                }
            }
        }

        if (!stack.isEmpty) {
            i = if (fromLast) {
                endIndex - 1
            } else {
                startIndex
            }

            while (true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break
                    }
                } else if (i >= endIndex) {
                    break
                }

                slot = slots[i]
                itemStack = slot.stack
                if (itemStack.isEmpty && slot.canInsert(stack)) {
                    if (stack.count > slot.maxItemCount) {
                        slot.insertStack(stack)
                        //slot.stack = stack.split(slot.maxItemCount)
                    } else {
                        slot.insertStack(stack)
                        //slot.stack = stack.split(stack.count)
                    }

                    slot.markDirty()
                    bl = true
                    break
                }

                if (fromLast) {
                    --i
                } else {
                    ++i
                }
            }
        }
        val setStacks = endIndex - startIndex
        val stackCopy = stack.copy()
        stackCopy.count = setStacks
        cursorStack = stackCopy
        return bl
    }
}