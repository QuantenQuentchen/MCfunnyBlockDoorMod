package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB
import kotlinx.coroutines.runBlocking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class quickAndDirtyTestBlock(settings: Settings): Block(settings) {

    override fun neighborUpdate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        sourceBlock: Block?,
        sourcePos: BlockPos?,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)

        val power = world?.getReceivedRedstonePower(pos) ?: return
        if (power != null) {
            if (power > 0) {
                if (pos != null) {
                    testEmit(world, pos)
                }
            } else {
                // The block is not receiving redstone power
            }
        }
    }

    companion object {
        private val voxelizedPrism = OBB.getEmittingGrid(0f, 90f, 0f).grid
    }

    private fun testEmit(world: World, pos: BlockPos){
        var counter = 0
        val blockStates = listOf(
            Blocks.DIAMOND_BLOCK.defaultState,
            Blocks.IRON_BLOCK.defaultState, //3
            Blocks.GOLD_BLOCK.defaultState, //2
            Blocks.EMERALD_BLOCK.defaultState,
            Blocks.REDSTONE_BLOCK.defaultState,
            Blocks.LAPIS_BLOCK.defaultState,
            Blocks.COAL_BLOCK.defaultState,
            Blocks.QUARTZ_BLOCK.defaultState,
            Blocks.NETHERITE_BLOCK.defaultState,
            Blocks.OBSIDIAN.defaultState,
            Blocks.CRYING_OBSIDIAN.defaultState,
            Blocks.COPPER_BLOCK.defaultState
        )

        for (xPlane in voxelizedPrism) {
            for ((idx,yPlane) in xPlane.withIndex()) {
                val blockState = blockStates[idx]
                for (zPlane in yPlane) {
                    counter++
                    val blockPos: BlockPos? = zPlane
                    if (blockPos == null) {
                        FunnyBlockDoorMod.logger.info("BlockPos is null")
                        continue
                    }
                    val absolutePos = pos.add(blockPos)
                    world.setBlockState(absolutePos, blockState)
                    FunnyBlockDoorMod.logger.info("Block placed with $blockPos at $absolutePos")
                }
            }
        }

        val xDimension = voxelizedPrism.size
        val yDimension = voxelizedPrism[0].size
        val zDimension = voxelizedPrism[0][0].size

        FunnyBlockDoorMod.logger.info("Dimensions of emittingPrism: x=$xDimension, y=$yDimension, z=$zDimension")
        FunnyBlockDoorMod.logger.info("Total blocks placed: $counter")
    }

}