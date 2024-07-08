package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.RedstoneEmitter
import funnyblockdoormod.funnyblockdoormod.block.custom.blockPlaceUtil
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterBlock.Companion.FACING
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterInventory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.baseWirelessRedstone
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.noEnergy
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviourFactory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneReciever
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockPos3DGrid
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.InventoryDepthChange
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World


class doorEmitterBlockEntity : BlockEntity, ExtendedScreenHandlerFactory, ImplementedInventory, IwirelessRedstoneReciever,
    InventoryDepthChange {

    companion object {

        //maybe init behaviour here
        var defaultEnergyBehaviourFactory: IenergyBehaviourFactory = noEnergy.Companion

        fun registerAssociates(entityType: BlockEntityType<doorEmitterBlockEntity>) {
            defaultEnergyBehaviourFactory.registerEnergyStorage(entityType)
        }

        private enum class ExtensionState {
            EXTENDING,
            EXTENDED,
            RETRACTING,
            RETRACTED
        }

    }

    constructor(pos: BlockPos, state: BlockState) : super(ModBlockEntities.DOOR_EMITTER_BLOCK_ENTITY, pos, state) {
        energyBehaviour.afterTypeCreation() // The fuck ?!?

        this.propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> invDepth
                    1 -> encodedSlotStates
                    2 -> energyBehaviour.getEnergy().toInt()
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> invDepth = value
                    1 -> encodedSlotStates = value
                }
            }

            override fun size(): Int {
                return 3
            }
        }
    }
    //Behaviours

    fun setEnergyStoragePropDel(value: Int) {
        propertyDelegate.set(2, value)
    }

    var energyBehaviour: IenergyBehaviour = defaultEnergyBehaviourFactory.create(this)

    private var wirelessRedstoneBehaviour: IwirelessRedstoneBehaviour = baseWirelessRedstone

    private fun getRecievingRedstone(state: BlockState, world: World, pos: BlockPos): Boolean {
        val facingDirection = state.get(FACING)
        return world.isEmittingRedstonePower(pos, facingDirection)
    }

    private fun updateRedstone(state: BlockState, world: World, pos: BlockPos) {

        val currentPoweredState = state.get(RedstoneEmitter.POWERED)
        val shouldPower = getRecievingRedstone(state, world, pos)

        if (shouldPower != currentPoweredState) {
            setCharged(shouldPower)
        }

    }

    init {
        val state = world?.getBlockState(pos)
        world?.let { updateRedstone(state!!, it, pos) }
        energyBehaviour.init()
        wirelessRedstoneBehaviour.init()
        subToChannel()
    }

    var inventory: doorEmitterInventory = doorEmitterInventory()

    protected val propertyDelegate: PropertyDelegate
    private var extensionState: ExtensionState = ExtensionState.RETRACTED
    private var invDepth: Int = 0
    private var encodedSlotStates: Int = 0

    private var blockDelay: Int = 10
    private var currentBlockDelay: Int = blockDelay

    private fun resetBlockDelay() {
        currentBlockDelay = blockDelay
    }

    private fun decrementBlockDelay() {
        if (currentBlockDelay <= 0) {
            resetBlockDelay()
            return
        }
        currentBlockDelay--
    }

    private fun cooldownOver(): Boolean {
        return currentBlockDelay <= 0
    }

    private var isCharged = false

    private var currentChannel: Int? = null

    private var lastTickTime = System.currentTimeMillis()
    private var currentXAngle: Float = 0f
    private var currentYAngle: Float = 0f
    private var currentZAngle: Float = 0f

    private var currentEmittingGrid = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)
    private var gridCache: BlockPos3DGrid? = null

    private var energyConsumptionPerBlockBase = 10

    private var energyConsumptionMultiplier = 1f

    private var energyConsumptionPerBlock = (energyConsumptionPerBlockBase * energyConsumptionMultiplier).toInt()

    private val placmentIterator = placementIteratorManager.fromNbt(null)

    //Behaviour

    fun setCharged(isCharged: Boolean) {
        if (isCharged) {
            setExtendingState()
        } else {
            setRetractingState()
        }
    }

    private fun setRetractingState() {
        if (extensionState == ExtensionState.RETRACTED) return
        extensionState = ExtensionState.RETRACTING
    }

    private fun setExtendingState() {
        if (extensionState == ExtensionState.EXTENDED) return
        extensionState = ExtensionState.EXTENDING
    }

    private fun setExtendedState() {
        extensionState = ExtensionState.EXTENDED
    }

    private fun setRetractedState() {
        extensionState = ExtensionState.RETRACTED
    }

    override fun getDisplayName(): Text {
        return Text.literal("Door Emitter") //change to translatable later
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf?) {
        buf?.writeBlockPos(this.pos)
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        nbt?.put("inventory", inventory.toNbt())
        nbt?.putInt("invDepth", invDepth)
        nbt?.putInt("channel", currentChannel ?: -1)
    }

    override fun markDirty() {
        super<BlockEntity>.markDirty()
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        inventory = doorEmitterInventory.fromNbt(nbt!!.get("inventory") as NbtCompound)
        invDepth = nbt.getInt("progress")
        currentChannel = nbt.getInt("channel")
        if(currentChannel != -1) subToChannel()
        else currentChannel = null
    }

    override fun getItems(): DefaultedList<ItemStack> {
        return DefaultedList.ofSize(2, ItemStack.EMPTY)
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        return playerInventory?.let { DoorEmitterScreenHandler(syncId, it, this, propertyDelegate) }
    }

    private fun getBlockStateFromItemStack(itemStack: ItemStack): BlockState? {
        val item = itemStack.item
        if (item is BlockItem) {
            val block: Block = item.block
            return block.defaultState
        }
        return null
    }

    fun applyRotation(angleXI: Int, angleYI: Int, angleZI: Int){
        val angleX = angleXI.toFloat()
        val angleY = angleYI.toFloat()
        val angleZ = angleZI.toFloat()
        if(angleX == currentXAngle && angleY == currentYAngle && angleZ == currentZAngle) return
        currentXAngle = angleX
        currentYAngle = angleY
        currentZAngle = angleZ
        setNewGrid()
    }

    private fun setNewGrid() {
        gridCache = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)
    }

    private fun applyGrid() {
        if(gridCache == null) return
        currentEmittingGrid = gridCache!!
        gridCache = null
    }


    private fun placeOverlayBlocks(world: World, pos: BlockPos, block: BlockState) {
    }

    private fun unplaceOverlayBlocks(world: World, pos: BlockPos, block: BlockState) {
    }

    fun modifyInvDepth(invDepthDelta: Int) {
        invDepth += invDepthDelta
        if (invDepth < 0) {
            invDepth = 24
        }
        if (invDepth > 24) {
            invDepth = 0
        }
        inventory.depth = invDepth
    }

    private fun encodeBlockedStates(blockedSlots: MutableSet<Int>): Int {
        var compundInt = 0
        for (i in blockedSlots) {
            compundInt = compundInt or (1 shl i)
        }
        return compundInt
    }

    fun setBlockedSlotsDelegate(blockedSlots: MutableSet<Int>) {
        this.propertyDelegate.set(1, encodeBlockedStates(blockedSlots))
    }

    private fun canOperate(): Boolean {
        if(!energyBehaviour.canConsume(energyConsumptionPerBlock)) return false
        if(!cooldownOver()) {
            decrementBlockDelay()
            return false
        }
        return true
    }

    private var ticks = 0
    private var switch = false
    private var x = 0
    private var y = 0
    private var z = 0
    private var i = 0
    fun tick(world: World, pos: BlockPos, state: BlockState) {

        if (world.isClient) return
        if (!world.isChunkLoaded(pos)) return


        val currentTickTime = System.currentTimeMillis()
        val operationMultiplier = (currentTickTime - lastTickTime) / 20

        //Tick Logic

        when(extensionState) {
            ExtensionState.EXTENDING -> {
                emittTick(world, pos)
            }

            ExtensionState.RETRACTING -> {
                retractTick(world, pos)
            }

            ExtensionState.RETRACTED -> {
                applyGrid()
            }

            ExtensionState.EXTENDED -> {
                //I dunno have fun, or notify extension Listeners ig, I dunno, but have it here for completion with ENUM States
            }
        }
        lastTickTime = currentTickTime
    }

    private fun debugOBBOffsets(world: World, pos: BlockPos) {
        val block = currentEmittingGrid.getBlock(Vec3i(x, y, z))

        if(block != null && !(block.blockPos.x == 0 && block.blockPos.y == 0 && block.blockPos.z == 0)){
            world.setBlockState(block.blockPos.add(pos), Blocks.QUARTZ_BLOCK.defaultState,3)
        }
        x++
        if(x > 4) {
            x = 0
            y++
        }
        if(y > 4) {
            y = 0
            x = 0
            z++
        }
        if(z > 25) {
            z = 0
            y = 0
            x = 0
        }

        i++
        if(i > currentEmittingGrid.bundleOffsets.size - 1){
            i = 0
        }

/*        for (block in currentEmittingGrid.bundleOffsets) {
            world.setBlockState(block.blockPos.add(pos), Blocks.QUARTZ_BLOCK.defaultState,3)
        }*/
    }

    private fun debugDrawOBB(world: World, obb: OBB, pos: BlockPos) {
        obb.debugDrawOBB(world, obb, pos)
    }

    private fun emittTick(world: World, emitterPos: BlockPos) {
        //TODO: Figure out Quartz Issue apparently overstepping bounds

        if(placmentIterator.hasNext()){

            val cords = placmentIterator.next() ?: return

            val item = inventory.getStackOrNull(cords)
            val pos = currentEmittingGrid.getBlock(cords)?.blockPos


            if(item != null && pos != null){
                val block = getBlockStateFromItemStack(item)
                if(block != null){
                    if(!canOperate()) return
                    energyBehaviour.consume(energyConsumptionPerBlock)
                    blockPlaceUtil.placeBlock(pos.add(emitterPos), world, block, false)
                }
            }
        } else{
            setExtendedState()
        }

    }

    private fun retractTick(world: World, emitterPos: BlockPos){

        if(placmentIterator.hasPrevious()){

            val cords = placmentIterator.previous()

            val item = inventory.getStackOrNull(cords) ?: return
            FunnyBlockDoorMod.logger.info("Item: $item")
            val pos = currentEmittingGrid.getBlock(cords)?.blockPos ?: return
            val block = getBlockStateFromItemStack(item) ?: return

            FunnyBlockDoorMod.logger.info("Pos: $item, $pos, $cords")

            if(!canOperate()) return
            energyBehaviour.consume(energyConsumptionPerBlock)
            blockPlaceUtil.removeBlock(pos.add(emitterPos), world, block, false)

        } else{
            setRetractedState()
        }

    }

    private fun subToChannel(){
        if (currentChannel == null) return
        wirelessRedstoneBehaviour.subscribeToChannel(currentChannel!!, this)
    }

    private fun unsubFromChannel(channel: Int){
        if (currentChannel == null) return
        wirelessRedstoneBehaviour.unsubscribeFromChannel(currentChannel!!, this)
    }

    fun setNewChannel(channel: Int){
        if (currentChannel == channel) return
        unsubFromChannel(currentChannel!!)
        currentChannel = channel
        subToChannel()
    }

    override fun onChannelChange(isActive: Boolean) {
        setCharged(isActive)
    }

    override fun onDepthChange(depth: Int) {
        when(depth){
            1 -> setBlockedSlotsDelegate(mutableSetOf(12))
            else -> setBlockedSlotsDelegate(mutableSetOf())
        }

    }

}