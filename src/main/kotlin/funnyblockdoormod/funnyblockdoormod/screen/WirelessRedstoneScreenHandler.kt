package funnyblockdoormod.funnyblockdoormod.screen

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IchangableChannel
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class WirelessRedstoneScreenHandler : ScreenHandler{


    lateinit var world: World
    lateinit var pos: BlockPos
    //lateinit var inv: PlayerInventory

    companion object {
        val CHANNEL_STATE_UPDATE_PACKAGE: Identifier = Identifier(FunnyBlockDoorMod.MOD_ID, "channel_state_update_package")
    }

    constructor(syncId: Int, inventory: PlayerInventory, buf: PacketByteBuf):
            this(syncId, inventory, buf.readBlockPos())

    constructor(syncId: Int, playerInventory: PlayerInventory, pos: BlockPos?)
            : super(ModScreenHandlers.WIRELESS_REDSTONE_SCREEN_HANDLER, syncId) {
        if (pos != null) {
            this.pos = pos
            this.world = playerInventory.player.world
        }

    }



    override fun quickMove(player: PlayerEntity?, slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    private fun sendStateUpdate(pos: BlockPos, channel: Int) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBlockPos(pos)
        buf.writeInt(channel)

        ClientPlayNetworking.send(CHANNEL_STATE_UPDATE_PACKAGE, buf)
    }

    fun setChannel(channel: Int) {
        sendStateUpdate(pos, channel)
/*        val blockState = world?.getBlockState(pos)
        if (blockState != null) {
            if (blockState.block is IchangableChannel) {
                if (world != null) {
                    if (pos != null) {
                        (blockState.block as IchangableChannel).setChannelState(world, pos, channel)
                    }
                }
            }
        }*/
    }

    fun getChannel(): Int? {
        val blockState = world.getBlockState(pos)
        if (blockState != null) {
            if (blockState.block is IchangableChannel) {
                return world.let { (blockState.block as IchangableChannel).getChannelState(it, pos) }
            }
        }
        return null
    }
}