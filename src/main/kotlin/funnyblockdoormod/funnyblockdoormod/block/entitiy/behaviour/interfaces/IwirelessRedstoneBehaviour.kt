package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

import net.minecraft.registry.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IwirelessRedstoneBehaviour {
    fun init()

    fun setChannel(channel: Int, isActive: Boolean)

    fun subscribeToChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever)

    fun subscribe(redstoneReciever: IwirelessRedstoneRecieverNum)

    fun subscribeAsActivatorPos(channel: Int, pos: BlockPos, dim: RegistryKey<World>)

    fun unsubscribeAsActivatorPos(channel: Int, pos: BlockPos, dim: RegistryKey<World>)

    fun subscribePosToChannel(channel: Int, pos: BlockPos, dim: RegistryKey<World>)

    fun unsubscribeFromChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever)

    fun unsubscribe(redstoneReciever: IwirelessRedstoneRecieverNum)

    fun unsubscribePosFromChannel(channel: Int, pos: BlockPos, dim: RegistryKey<World>)

}