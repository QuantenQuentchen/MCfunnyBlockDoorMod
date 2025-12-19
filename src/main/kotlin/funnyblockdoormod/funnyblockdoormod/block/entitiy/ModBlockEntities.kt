package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModBlockEntities {

    val DOOR_EMITTER_BLOCK_ENTITY: BlockEntityType<DoorEmitterBlockEntity> = registerDoorEmitterBlockEntity()
    val FORCE_FIELD_BLOCK_ENTITY: BlockEntityType<ForceFieldBlockEntity> = registerForceFieldBlockEntity()

    private fun registerDoorEmitterBlockEntity(): BlockEntityType<DoorEmitterBlockEntity> {
        val be = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(FunnyBlockDoorMod.MOD_ID, "door_emitter"),
            FabricBlockEntityTypeBuilder.create(
                ::DoorEmitterBlockEntity,
                ModBlocks.DOOREMITTER
            ).build()
        )
        DoorEmitterBlockEntity.registerAssociates(be)
        return be
    }
    private fun registerForceFieldBlockEntity(): BlockEntityType<ForceFieldBlockEntity> {
        val be = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(FunnyBlockDoorMod.MOD_ID, "force_field"),
            FabricBlockEntityTypeBuilder.create(
                ::ForceFieldBlockEntity,
                ModBlocks.FORCEFIELD
            ).build()
        )
        //ForceFieldBlockEntity.registerAssociates(be)
        return be
    }
    
    fun registerBlockEntities() {
        FunnyBlockDoorMod.logger.info("Registering block entities for " + FunnyBlockDoorMod.MOD_ID)
        // Register your block entities here
    }



}