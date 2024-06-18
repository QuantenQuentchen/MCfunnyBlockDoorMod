package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.entitiy.ModBlockEntities
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.teamRebornEnergy
import funnyblockdoormod.funnyblockdoormod.block.entitiy.doorEmitterBlockEntity
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class doorEmitterBlock(settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity {
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
        //var energy = ((world.getBlockEntity(pos!!) as doorEmitterBlockEntity).energyBehaviour as teamRebornEnergy).energyStorage.amount
        //FunnyBlockDoorMod.logger.info("Energy: $energy")
        //FunnyBlockDoorMod.logger.info("Block used")
        return ActionResult.SUCCESS

    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        super.neighborUpdate(state, world, pos, block, neighborPos, moved)

        val isPowered = world.isReceivingRedstonePower(pos)
        val blockEntity = world.getBlockEntity(pos)

        if (blockEntity is doorEmitterBlockEntity) {
            blockEntity.setCharged(isPowered)
        }
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