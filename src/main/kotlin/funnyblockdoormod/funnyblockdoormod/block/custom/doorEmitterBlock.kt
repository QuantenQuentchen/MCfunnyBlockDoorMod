package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.block.entitiy.ModBlockEntities
import funnyblockdoormod.funnyblockdoormod.block.entitiy.doorEmitterBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class doorEmitterBlock(settings: FabricBlockSettings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return doorEmitterBlockEntity(pos!!, state!!)
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
            if(blockEntity is doorEmitterBlockEntity){
                ItemScatterer.spawn(world, pos, blockEntity)
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
            if(blockEntity != null && blockEntity is doorEmitterBlockEntity){
                player?.openHandledScreen(blockEntity)
            }
        }

        return ActionResult.SUCCESS

    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return checkType(type, ModBlockEntities.DOOR_EMITTER_BLOCK_ENTITY
        ) { world1: World, pos: BlockPos, state1: BlockState, blockEntity: doorEmitterBlockEntity ->
            blockEntity.tick(world1, pos, state1)
        }
    }

}