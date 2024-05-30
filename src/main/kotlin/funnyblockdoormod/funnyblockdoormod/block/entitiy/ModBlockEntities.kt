package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModBlockEntities {

    val DOOR_EMITTER_BLOCK_ENTITY: BlockEntityType<doorEmitterBlockEntity> = registerDoorEmitterBlockEntity()
    
    private fun registerDoorEmitterBlockEntity(): BlockEntityType<doorEmitterBlockEntity> {
        val be = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier(FunnyBlockDoorMod.MOD_ID, "door_emitter"),
            FabricBlockEntityTypeBuilder.create(
                ::doorEmitterBlockEntity,
                ModBlocks.DOOREMITTER
            ).build()
        )
        doorEmitterBlockEntity.registerAssociates(be)
        return be
    }
    
    fun registerBlockEntities() {
        FunnyBlockDoorMod.logger.info("Registering block entities for " + FunnyBlockDoorMod.MOD_ID)
        // Register your block entities here
    }



}