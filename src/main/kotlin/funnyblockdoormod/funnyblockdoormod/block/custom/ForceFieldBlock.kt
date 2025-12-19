package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.block.entitiy.ModBlockEntities
import funnyblockdoormod.funnyblockdoormod.block.entitiy.ForceFieldBlockEntity
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IConnectable
import funnyblockdoormod.funnyblockdoormod.screenhandler.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.screenhandler.ForceFieldScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class ForceFieldBlock(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider, IConnectable {

    companion object{
        val FACING: DirectionProperty = Properties.FACING
        val POWERED: BooleanProperty = Properties.POWERED
    }

    init {
        defaultState = (stateManager.defaultState as BlockState)
            .with(FACING, Direction.NORTH)
            .with(POWERED, false)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        // Get the side of the block that was clicked
        val clickedFace = ctx.side

        // Face away from the clicked surface (opposite direction)
        val facingDirection = clickedFace//.opposite

        // If it's vertical (top/bottom), default to player's horizontal facing
        /*
        val horizontalFacing = if (facingDirection.axis.isHorizontal) {
            facingDirection
        } else {
            ctx.playerFacing.opposite
        }
        */

        return this.defaultState.with(FACING, facingDirection)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(POWERED)
        super.appendProperties(builder)
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
        return ForceFieldBlockEntity(pos!!, state!!)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun onStateReplaced(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        newState: BlockState?,
        moved: Boolean
    ) {

        if(state?.block != newState?.block){
            val blockEntity = world?.getBlockEntity(pos)
            if(blockEntity is ForceFieldBlockEntity){
                //ItemScatterer.spawn(world, pos, blockEntity)
                world.updateComparators(pos, this)
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {

        if(!world!!.isClient){
            val blockEntity = world.getBlockEntity(pos)
            if(blockEntity != null && blockEntity is ForceFieldBlockEntity){
                player?.openHandledScreen(blockEntity)
            }
        }
        //var energy = ((world.getBlockEntity(pos!!) as ForceFieldBlockEntity).energyBehaviour as teamRebornEnergy).energyStorage.amount
        //FunnyBlockDoorMod.logger.info("Energy: $energy")
        //FunnyBlockDoorMod.logger.info("Block used")
        return ActionResult.SUCCESS

    }

    private fun getRecievingRedstone(state: BlockState, world: World, pos: BlockPos): Boolean {
        val facingDirection = state.get(FACING)
        return world.isEmittingRedstonePower(pos, facingDirection)
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        super.neighborUpdate(state, world, pos, block, neighborPos, moved)

        val currentPoweredState = state.get(POWERED)
        val shouldPower = getRecievingRedstone(state, world, pos)
        val blockEntity = world.getBlockEntity(pos)

        if (shouldPower != currentPoweredState) {
            world.setBlockState(pos, state.with(POWERED, shouldPower), 2)
            if (blockEntity is ForceFieldBlockEntity) {
                //blockEntity.setCharged(shouldPower)
            }
        }

    }


    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return checkType(type, ModBlockEntities.FORCE_FIELD_BLOCK_ENTITY
        ) { world1: World, pos: BlockPos, state1: BlockState, blockEntity: ForceFieldBlockEntity ->
            blockEntity.tick(world1, pos, state1)
        }
    }


    override fun canConnect(direction: Direction, state: BlockState): Boolean {
        return state.get(FACING) == direction
    }

}