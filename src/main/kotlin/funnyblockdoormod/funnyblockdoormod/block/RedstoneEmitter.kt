package funnyblockdoormod.funnyblockdoormod.block

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.annotations.ServerSideOnlyINFO
import funnyblockdoormod.funnyblockdoormod.block.RedstoneReciever.Companion
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.baseWirelessRedstone
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IchangableChannel
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IConnectable
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.screen.ModScreenHandlers
import funnyblockdoormod.funnyblockdoormod.screen.WirelessRedstoneScreenHandler
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.TestableWorld
import net.minecraft.world.World

class RedstoneEmitter(settings: Settings): Block(settings), IchangableChannel, IConnectable {

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        val POWERED: BooleanProperty = Properties.POWERED
        val CHANNEL: IntProperty = IntProperty.of("channel", 0, 256)
        val redstoneBehaviour = baseWirelessRedstone
    }

    var cachePos: BlockPos? = null

    init {
        defaultState = (stateManager.defaultState as BlockState)
            .with(FACING, Direction.NORTH)
            .with(POWERED, false)
            .with(CHANNEL, 0)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val playerFacing = ctx.playerLookDirection.opposite
        val horizontalFacing = if (playerFacing.axis.isHorizontal) playerFacing else Direction.NORTH
        return this.defaultState.with(FACING, horizontalFacing)
    }

    @Deprecated("Deprecated in Java")
    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        FunnyBlockDoorMod.logger.info("Neighbor update on Emitter with: $pos")
        val currentPoweredState = state.get(POWERED)
        val shouldPower = getRecievingRedstone(state, world, pos)
        if (shouldPower != currentPoweredState) {
            world.setBlockState(pos, state.with(POWERED, shouldPower), 2)
            redstoneBehaviour.setChannel(state.get(CHANNEL), shouldPower)
        }
    }

    private fun getRecievingRedstone(state: BlockState, world: World, pos: BlockPos): Boolean {
        val facingDirection = state.get(FACING)
        return world.isEmittingRedstonePower(pos, facingDirection)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(POWERED)
        builder.add(CHANNEL)
        super.appendProperties(builder)
    }

    private fun getFacing(state: BlockState): Direction {
        return state.get(FACING)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun emitsRedstonePower(state: BlockState): Boolean {
        return false
    }

/*    @Deprecated("Deprecated in Java", ReplaceWith("if (state.get(Properties.POWERED)) 15 else 0"))
    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if (state.get(Properties.HORIZONTAL_FACING) == facing.opposite) 15 else 0
    }*/

/*    @Deprecated("Deprecated in Java",
        ReplaceWith("if (state.get(Properties.POWERED) && getFacing(state) == direction) 15 else 0")
    )
    override fun getStrongRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (state.get(Properties.POWERED) && getFacing(state) == direction) 15 else 0
    }*/

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (!world.isClient) {
            player.openHandledScreen(object : ExtendedScreenHandlerFactory {

                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                    return WirelessRedstoneScreenHandler(syncId, inv, pos)
                }

                override fun getDisplayName(): Text {
                    return Text.of("Redstone Emitter")
                }

                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeBlockPos(pos)
                }
            })
        }
        return ActionResult.SUCCESS
    }

    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun hasComparatorOutput(state: BlockState): Boolean {
        return true
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        // Implement your logic here to determine the comparator output
        return 0
    }

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if(world == null || pos == null || state == null) return

        if(!world.isClient){
            redstoneBehaviour.subscribeAsActivatorPos(state.get(CHANNEL), pos, world.registryKey)
        }

        val currentPoweredState = state.get(POWERED)
        val shouldPower = getRecievingRedstone(state, world, pos)
        if (shouldPower != currentPoweredState) {
            world.setBlockState(pos, state.with(POWERED, shouldPower), 2)
            redstoneBehaviour.setChannel(state.get(CHANNEL), shouldPower)
        }
        super.onPlaced(world, pos, state, placer, itemStack)
    }

    override fun onBreak(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        player: PlayerEntity?
    ) {
        if(world != null && pos != null && state != null){
            if(!world.isClient){
                redstoneBehaviour.unsubscribeAsActivatorPos(state.get(CHANNEL), pos, world.registryKey)
            }
        }
        super.onBreak(world, pos, state, player)
    }

    @ServerSideOnlyINFO
    override fun setChannelState(world: World, pos: BlockPos, channel: Int) {
        val state = world.getBlockState(pos)
        if(state.get(CHANNEL) == channel) return
        redstoneBehaviour.unsubscribeAsActivatorPos(state.get(CHANNEL), pos, world.registryKey)
        val newState = state.with(CHANNEL, channel)
        world.setBlockState(pos, newState, NOTIFY_NEIGHBORS or 2)
        redstoneBehaviour.subscribeAsActivatorPos(channel, pos, world.registryKey)
    }

    override fun getChannelState(world: World, pos: BlockPos): Int {
        val state = world.getBlockState(pos)
        return state.get(CHANNEL)
    }

    override fun canConnect(direction: Direction, state: BlockState): Boolean {
        return state.get(FACING) == direction.opposite
    }

}