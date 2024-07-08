package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.funnyDoorPersistantState
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object blockPlaceUtil {

    private var noBreakList: funnyDoorPersistantState? = null

    private fun posIsPlaceable(pos: BlockPos, world: World): Boolean {
        return world.isAir(pos)
        //world.getBlockState(pos).block.isAir(world.getBlockState(pos), world, pos)
    }


    fun placeBlock(pos: BlockPos, world: World, state: BlockState, isIndestructible: Boolean = false) {
        if(noBreakList == null) noBreakList = world.server?.let { funnyDoorPersistantState.getServerState(it) }
        if (posIsPlaceable(pos, world)) {
            world.setBlockState(pos, state)
            //null placer could result in exception in other mods, look into this
            state.block.onPlaced(world, pos, state, null, ItemStack(state.block))

            val placeSound: SoundEvent = state.block.getSoundGroup(state).placeSound
            world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0f, 1.0f)

            if(isIndestructible) noBreakList!!.addBlock(pos)

        }
    }

    private fun isSameBlock(pos: BlockPos, world: World, state: BlockState): Boolean {
        return world.getBlockState(pos).block == state.block
    }

    fun removeBlock(pos: BlockPos, world: World, state: BlockState, isIndestructible: Boolean = false) {
        if(noBreakList == null) noBreakList = world.server?.let { funnyDoorPersistantState.getServerState(it) }
        if (posIsPlaceable(pos, world)) return
        if (!isSameBlock(pos, world, state)) return
        if(isIndestructible) {
            noBreakList!!.removeBlock(pos)
        }

        world.removeBlock(pos, false)
        val breakSound: SoundEvent = state.block.getSoundGroup(state).breakSound
        world.playSound(null, pos, breakSound, SoundCategory.BLOCKS, 1.0f, 1.0f)

    }

}