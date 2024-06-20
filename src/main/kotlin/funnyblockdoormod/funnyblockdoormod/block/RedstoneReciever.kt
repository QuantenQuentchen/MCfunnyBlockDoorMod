package funnyblockdoormod.funnyblockdoormod.block

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.RedstoneEmitter.Companion
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.baseWirelessRedstone
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IchangableChannel
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneReciever
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneRecieverNum
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IConnectable
import funnyblockdoormod.funnyblockdoormod.screen.WirelessRedstoneScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.RedstoneWireBlock
import net.minecraft.block.RepeaterBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
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
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.rmi.registry.Registry

class RedstoneReciever(settings: Settings): Block(settings), IwirelessRedstoneRecieverNum, IchangableChannel,
    IConnectable {

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        val POWERED: BooleanProperty = Properties.POWERED
        val CHANNEL: IntProperty = IntProperty.of("channel", 0, 256)
        val redstoneBehaviour = baseWirelessRedstone
    }

    init {
        defaultState = (stateManager.defaultState as BlockState)
            .with(FACING, Direction.NORTH)
            .with(POWERED, false)
            .with(CHANNEL, 0)
        redstoneBehaviour.subscribe(this)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(POWERED)
        builder.add(CHANNEL)
        super.appendProperties(builder)
    }


    private fun setPoweredState(world: World, pos: BlockPos, powered: Boolean) {
        val state = world.getBlockState(pos)
        if (state.block is RedstoneReciever) {
            val newState = state.with(POWERED, powered)
            world.setBlockState(pos, newState, NOTIFY_NEIGHBORS or 2)
        }
    }

    override fun setChannelState(world: World, pos: BlockPos, channel: Int) {
        FunnyBlockDoorMod.logger.info("Setting channel to $channel")
        val state = world.getBlockState(pos)
        redstoneBehaviour.unsubscribePosFromChannel(state.get(CHANNEL), pos, world.registryKey)
        if (state.block is RedstoneReciever) {
            val newState = state.with(CHANNEL, channel)
            world.setBlockState(pos, newState, NOTIFY_NEIGHBORS or 2)
        }
        redstoneBehaviour.subscribePosToChannel(channel, pos, world.registryKey)
    }

    override fun getChannelState(world: World, pos: BlockPos): Int {
        val state = world.getBlockState(pos)
        return state.get(CHANNEL)
    }

    @Deprecated("Deprecated in Java")
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

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if(world != null && pos != null && state != null){
            if(!world.isClient){
                redstoneBehaviour.subscribePosToChannel(state.get(CHANNEL), pos, world.registryKey)
                setPoweredState(world, pos, redstoneBehaviour.getChannel(state.get(CHANNEL)))
            }
        }
        super.onPlaced(world, pos, state, placer, itemStack)
    }

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        if(world != null && pos != null && state != null){
            Blocks.REDSTONE_WIRE
            if(!world.isClient) redstoneBehaviour.unsubscribePosFromChannel(state.get(CHANNEL), pos, world.registryKey)
        }
        super.onBreak(world, pos, state, player)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val playerFacing = ctx.playerLookDirection.opposite
        val horizontalFacing = if (playerFacing.axis.isHorizontal) playerFacing else Direction.NORTH
        return this.defaultState.with(RedstoneEmitter.FACING, horizontalFacing)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL
    }

/*    @Deprecated("Deprecated in Java", ReplaceWith("if (state.get(FACING) == facing && state.get(POWERED)) 15 else 0"))
    override fun getStrongRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if (state.get(FACING) == facing && state.get(POWERED)) 15 else 0
    }*/

    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun emitsRedstonePower(state: BlockState): Boolean {
        return true
    }

    @Deprecated("Deprecated in Java", ReplaceWith("if (state.get(Properties.POWERED)) 15 else 0"))
    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if (state.get(FACING).opposite == facing && state.get(POWERED)) 15 else 0
    }

    @Deprecated("Deprecated in Java", ReplaceWith("true"))
    override fun hasComparatorOutput(state: BlockState): Boolean {
        return false
    }



/*    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        // Implement your logic here to determine the comparator output
        return 0
    }*/

    override fun onChannelChange(isActive: Boolean, pos: BlockPos, world: World) {
        setPoweredState(world, pos, isActive)
    }

    override fun canConnect(direction: Direction, state: BlockState): Boolean {
        return state.get(FACING) == direction.opposite
    }

}