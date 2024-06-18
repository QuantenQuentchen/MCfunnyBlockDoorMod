package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IwirelessRedstoneRecieverNum {
    fun onChannelChange(isActive: Boolean, pos: BlockPos, world: World)
}