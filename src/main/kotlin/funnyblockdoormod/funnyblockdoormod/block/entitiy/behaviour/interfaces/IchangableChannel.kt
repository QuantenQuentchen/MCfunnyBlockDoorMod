package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IchangableChannel {

    fun setChannelState(world: World, pos: BlockPos, channel: Int)

    fun getChannelState(world: World, pos: BlockPos): Int

}