package funnyblockdoormod.funnyblockdoormod.block.entitiy

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


class doorEmitterBlockEntity : BlockEntity, ExtendedScreenHandlerFactory, ImplementedInventory, IwirelessRedstoneReciever {

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
        energyBehaviour.afterTypeCreation()

        this.propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> progress
                    1 -> maxProgress
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> progress = value
                    1 -> maxProgress = value
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
    private var progress: Int = 0
    private var maxProgress: Int = 73

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
    private var currentGridBlocks = BlockState3DGrid(25, 25, 25)

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
        nbt?.putInt("progress", progress)
    }

    override fun markDirty() {
        super<BlockEntity>.markDirty()
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        inventory = doorEmitterInventory.fromNbt(nbt!!.get("inventory") as NbtCompound)
        progress = nbt.getInt("progress") ?: 0
    }

    override fun getItems(): DefaultedList<ItemStack> {
        return DefaultedList.ofSize(2, ItemStack.EMPTY)
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        return playerInventory?.let { DoorEmitterScreenHandler(syncId, it, this, propertyDelegate) }
    }

    fun getBlockStateFromItemStack(itemStack: ItemStack): BlockState? {
        val item = itemStack.item
        if (item is BlockItem) {
            val block: Block = item.block
            return block.defaultState
        }
        return null
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

}