package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.custom.blockPlaceUtil
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterInventory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.baseWirelessRedstone
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.noEnergy
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviourFactory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneReciever
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockState3DGrid
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.InventoryDepthChange
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
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
import net.minecraft.world.World


class doorEmitterBlockEntity : BlockEntity, ExtendedScreenHandlerFactory, ImplementedInventory, IwirelessRedstoneReciever,
    InventoryDepthChange{

    companion object {
        private val INPUT_SLOT = 0
        private val OUTPUT_SLOT = 1
        //maybe init behaviour here
        var defaultEnergyBehaviourFactory: IenergyBehaviourFactory = noEnergy.Companion

        fun registerAssociates( entityType: BlockEntityType<doorEmitterBlockEntity>){
            defaultEnergyBehaviourFactory.registerEnergyStorage(entityType)
        }
    }

    constructor(pos: BlockPos, state: BlockState) : super(ModBlockEntities.DOOR_EMITTER_BLOCK_ENTITY, pos, state){
        energyBehaviour.afterTypeCreation() // The fuck ?!?

        this.propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> invDepth
                    1 -> encodedSlotStates
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
                return 2
            }
        }
    }
    //Behaviours

    var energyBehaviour: IenergyBehaviour = defaultEnergyBehaviourFactory.create(this)

    private var wirelessRedstoneBehaviour: IwirelessRedstoneBehaviour = baseWirelessRedstone

    init {
        energyBehaviour.init()
        wirelessRedstoneBehaviour.init()
    }

    var inventory: doorEmitterInventory = doorEmitterInventory()

    protected val propertyDelegate: PropertyDelegate
    private var isEmitting = false
    private var isRetracting = false
    private var invDepth: Int = 0
    private var encodedSlotStates: Int = 0

    private var blockDelay: Int = 10
    private var currentBlockDelay: Int = blockDelay

    private fun resetBlockDelay() {
        currentBlockDelay = blockDelay
    }

    private fun decrementBlockDelay() {
        if(currentBlockDelay <= 0) {
            resetBlockDelay()
            return
        }
        currentBlockDelay--
    }

    private fun canOperate(): Boolean {
        return currentBlockDelay <= 0
    }

    private var isCharged = false

    private var redstoneActivationBehaviour = false

    private var lastTickTime = System.currentTimeMillis()
    private val currentXAngle: Float = 0f
    private val currentYAngle: Float = 0f
    private val currentZAngle: Float = 0f

    private var currentEmittingGrid = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)

    private var energyConsumptionPerBlockBase = 10

    private var energyConsumptionMultiplier = 1f

    private var energyConsumptionPerBlock = (energyConsumptionPerBlockBase * energyConsumptionMultiplier).toInt()

    //Behaviour

    fun setCharged(isCharged: Boolean) {
        this.isCharged = isCharged
    }

    override fun getDisplayName(): Text {
        return Text.literal("Door Emitter") //change to translatable later
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf?) {
        if (buf != null) {
            buf.writeBlockPos(this.pos)
        }
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        nbt?.put("inventory", inventory.toNbt())
        nbt?.putInt("invDepth", invDepth)
    }

    override fun markDirty() {
        super<BlockEntity>.markDirty()
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        inventory = doorEmitterInventory.fromNbt(nbt!!.get("inventory") as NbtCompound)
        invDepth = nbt.getInt("progress") ?: 0
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

    fun modifyInvDepth(invDepthDelta: Int) {
        invDepth += invDepthDelta
        if(invDepth < 0){
            invDepth = 24
        }
        if(invDepth > 24){
            invDepth = 0
        }
        inventory.depth = invDepth
    }

    private fun encodeBlockedStates(blockedSlots: MutableSet<Int>): Int{
        var compundInt = 0
        for (i in blockedSlots){
            compundInt = compundInt or (1 shl i)
        }
        return compundInt
    }

    fun setBlockedSlotsDelegate(blockedSlots: MutableSet<Int>){
        this.propertyDelegate.set(1, encodeBlockedStates(blockedSlots))
    }

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if(world.isClient) return
        if(!canOperate()) return
        if(!energyBehaviour.canConsume(energyConsumptionPerBlock)) return

        energyBehaviour.consume(energyConsumptionPerBlock)
        val currentTickTime = System.currentTimeMillis()
        val operationMultiplier = (currentTickTime - lastTickTime) / 20

        //Tick Logic

        if(isEmitting){
            emittTick(world, pos)
        }

        if(isRetracting){
            retractTick(world, pos)
        }

        //End Tick Logic
        decrementBlockDelay()
        lastTickTime = currentTickTime
    }

    private fun emittTick(world: World, emitterPos: BlockPos){
        if(inventory.iterator().hasNext() && currentEmittingGrid.iterator().hasNext()){
            val item = inventory.iterator().next()
            val pos = currentEmittingGrid.iterator().next()
            if(item != null && pos != null){
                //place block
                val block = getBlockStateFromItemStack(item)
                if(block != null){
                    blockPlaceUtil.placeBlock(pos.add(emitterPos), world, block, false)
                }
            }
        }
    }

    private fun retractTick(world: World, emitterPos: BlockPos){
        if(inventory.reverseIterator().hasNext() && currentEmittingGrid.reverseIterator().hasNext()){
            val item = inventory.reverseIterator().next()
            val pos = currentEmittingGrid.reverseIterator().next()
            if(item != null && pos != null){
                val block = getBlockStateFromItemStack(item)
                if(block != null){
                    blockPlaceUtil.removeBlock(pos.add(emitterPos), world, block, false)
                }
            }
        }
    }

    private fun setEmittingState() {

        return
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