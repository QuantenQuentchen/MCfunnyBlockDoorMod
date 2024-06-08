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
import net.minecraft.block.Blocks
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
import net.minecraft.util.math.Vec3d
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

    fun setEnergyStoragePropDel(value: Int){
        propertyDelegate.set(2, value)
    }

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
    private var currentXAngle: Float = 0f
    private var currentYAngle: Float = 0f
    private var currentZAngle: Float = 0f

    private var currentEmittingGrid = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)
    private var currentOBB = OBB.getRotatedOBB(currentXAngle, currentYAngle, currentZAngle)

    private var energyConsumptionPerBlockBase = 10

    private var energyConsumptionMultiplier = 1f

    private var energyConsumptionPerBlock = (energyConsumptionPerBlockBase * energyConsumptionMultiplier).toInt()

    //Behaviour

    fun setCharged(isCharged: Boolean) {
        this.isCharged = isCharged
        if(isCharged){
            isEmitting = true
            isRetracting = false
        } else {
            isRetracting = true
            isEmitting = false
        }
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

    private var ticks = 0
    private var switch = false
    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if(world.isClient) return
        if(!world.isChunkLoaded(pos)) return
        /*if(!canOperate()) {
            decrementBlockDelay()
            return
        }
        if(!energyBehaviour.canConsume(energyConsumptionPerBlock)) return


        val currentTickTime = System.currentTimeMillis()
        val operationMultiplier = (currentTickTime - lastTickTime) / 20*/

        //Tick Logic



       /* if(isEmitting){
            energyBehaviour.consume(energyConsumptionPerBlock)
            emittTick(world, pos)
        }

        if(isRetracting){
            //energyBehaviour.consume(energyConsumptionPerBlock)
            //retractTick(world, pos)
        }
*/
        //End Tick Logic
        ticks++
        if(ticks >= 50 && switch){
            world.players.forEach { player -> player.sendMessage(Text.of("Emitting x:$currentXAngle, y:$currentYAngle, z:$currentZAngle")) }
            emittTick(world, pos)
            switch = false
            ticks = 0
        }
        if(ticks >= 50 && !switch){
            world.players.forEach { player -> player.sendMessage(Text.of("Retracting x:$currentXAngle, y:$currentYAngle, z:$currentZAngle")) }
            testretractTikc(world, pos)
           /* if(currentXAngle >= 180){
                currentXAngle = 0f
                currentYAngle += 1
            }*/
            if(currentYAngle >= 180){
                currentYAngle = 0f
                currentZAngle += 1
            }
            if(currentZAngle >= 180) {
                world.players.forEach() { player -> player.sendMessage(Text.of("Test Complete")) }
            }
            currentYAngle += 1
            currentEmittingGrid = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)
            currentOBB = OBB.getRotatedOBB(currentXAngle, currentYAngle, currentZAngle)
            currentOBB.debugDrawOBB(world, currentOBB, pos)
            switch = true
            ticks = 0
        }
        //lastTickTime = currentTickTime
    }

    private fun emittTick(world: World, emitterPos: BlockPos){

        val testBlocks: MutableList<BlockState> = mutableListOf(
            Blocks.AMETHYST_BLOCK.defaultState,
            Blocks.DIAMOND_BLOCK.defaultState,
            Blocks.EMERALD_BLOCK.defaultState,
            Blocks.GOLD_BLOCK.defaultState,
            Blocks.IRON_BLOCK.defaultState,
            Blocks.LAPIS_BLOCK.defaultState,
            Blocks.REDSTONE_BLOCK.defaultState,
            Blocks.COAL_BLOCK.defaultState,
            Blocks.QUARTZ_BLOCK.defaultState,
            Blocks.NETHERITE_BLOCK.defaultState,
            Blocks.BONE_BLOCK.defaultState,
            Blocks.BRICKS.defaultState,
            Blocks.ACACIA_LEAVES.defaultState,
            Blocks.ACACIA_LOG.defaultState,
            Blocks.ACACIA_PLANKS.defaultState,
            Blocks.ACACIA_SLAB.defaultState,
            Blocks.ACACIA_STAIRS.defaultState,
            Blocks.ACACIA_TRAPDOOR.defaultState,
            Blocks.JUKEBOX.defaultState,
            Blocks.BOOKSHELF.defaultState,
            Blocks.CRAFTING_TABLE.defaultState,
            Blocks.FURNACE.defaultState,
            Blocks.LOOM.defaultState,
            Blocks.SMOKER.defaultState,
            Blocks.BLAST_FURNACE.defaultState,
            Blocks.CARTOGRAPHY_TABLE.defaultState,
            Blocks.FLETCHING_TABLE.defaultState,
            Blocks.SMITHING_TABLE.defaultState,
            Blocks.STONECUTTER.defaultState,
            Blocks.BREWING_STAND.defaultState,
            Blocks.ENCHANTING_TABLE.defaultState,
            Blocks.ANVIL.defaultState,
            Blocks.CHIPPED_ANVIL.defaultState,
            Blocks.DAMAGED_ANVIL.defaultState,
            Blocks.GRINDSTONE.defaultState,
            Blocks.BELL.defaultState,
            Blocks.CAMPFIRE.defaultState,
            Blocks.SOUL_CAMPFIRE.defaultState,
            Blocks.LANTERN.defaultState,
            Blocks.SOUL_LANTERN.defaultState,
            Blocks.TORCH.defaultState,
            Blocks.SOUL_TORCH.defaultState,
            Blocks.WALL_TORCH.defaultState,
            Blocks.SOUL_WALL_TORCH.defaultState,
            Blocks.END_ROD.defaultState,
            Blocks.REDSTONE_LAMP.defaultState,
        )

        //val blockPosToBlockStateList = currentOBB.orderBlocksByOBBRotation()

/*        for ((blockPos, blockState) in blockPosToBlockStateList) {
            // You can now access each BlockPos and BlockState
            // For example, to print them:
            world.setBlockState(blockPos.add(emitterPos), blockState)
            //println("BlockPos: $blockPos, BlockState: $blockState")
        }*/

/*        var origin = Vec3d(emitterPos.x.toDouble(), emitterPos.y.toDouble(), emitterPos.z.toDouble())

*//*        val blockPosList = currentOBB.getBlockPlane(origin)

        for(i in 0..4){
            for(j in 0..4){
                val blockPos = blockPosList[i][j]
                if(blockPos == emitterPos) continue
                world.setBlockState(blockPos, Blocks.GLOWSTONE.defaultState)
                FunnyBlockDoorMod.logger.info("BlockPos: $blockPos")
            }
        }*//*

    for(i in 0..24){
        val blockstate = testBlocks[i]

        val blockPosList = currentOBB.getBlockPlane(origin, i)

        for(k in 0..4){
            for(j in 0..4){
                val blockPos = blockPosList[k][j]
                if(blockPos == emitterPos) continue
                world.setBlockState(blockPos, blockstate)
                FunnyBlockDoorMod.logger.info("BlockPos: $blockPos")
            }
        }

        //origin = currentOBB.incrementByDirectionVec(origin)
        }*/

        val list = currentEmittingGrid.buildGrid()
        for ((idx,blockBundle) in list.withIndex()){
            val blockPos = blockBundle.blockPos
            val blockState = testBlocks[idx / 25]
            world.setBlockState(blockPos.add(emitterPos), blockState)
        }

       /*for(z in 0..24){
            val block = testBlocks[z % testBlocks.size]
            for(y in 0..24){
                for(x in 0..24){
                    val pos = currentEmittingGrid.getBlock(x,y,z)?.blockPos
                    if(pos != null){
                        //val item = inventory.getStack(x, y, z)
                        //if(item != null){
                            //val block = Blocks.AMETHYST_BLOCK.defaultState//getBlockStateFromItemStack(item)
                            if(block != null){
                                blockPlaceUtil.placeBlock(pos.add(emitterPos), world, block, false)
                            }
                        //}
                    }
                }
            }
        }*/
        isEmitting = false
    }

    private fun testretractTikc(world: World, emitterPos: BlockPos){
        for(z in -24..24){
            for(y in -24..24){
                for(x in -24..24){
                    if(x == 0 && y == 0 && z == 0) continue
                    world.setBlockState(BlockPos(x, y, z).add(emitterPos), Blocks.AIR.defaultState)
                }
            }
        }
        isRetracting = false
    }

    private fun retractTick(world: World, emitterPos: BlockPos){
        if(inventory.reverseIterator().hasNext() && currentEmittingGrid.reverseIterator().hasNext()){
            val item = inventory.reverseIterator().next()
            val pos = currentEmittingGrid.reverseIterator().next()?.blockPos
            if(item != null && pos != null){
                val block = getBlockStateFromItemStack(item)
                if(block != null){
                    blockPlaceUtil.removeBlock(pos.add(emitterPos), world, block, false)
                }
            }
        } else{
            //reset
            isRetracting = false
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