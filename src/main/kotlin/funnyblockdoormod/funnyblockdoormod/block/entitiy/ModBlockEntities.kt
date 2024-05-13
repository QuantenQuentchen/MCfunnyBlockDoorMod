package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModBlockEntities {

    val DOOR_EMITTER_BLOCK_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        "funnyblockdoormod:door_emitter",
        FabricBlockEntityTypeBuilder.create(
            ::doorEmitterBlockEntity,
            ModBlocks.DOOREMITTER
        ).build()
    )

    fun registerBlockEntities() {
        FunnyBlockDoorMod.logger.info("Registering block entities for " + FunnyBlockDoorMod.MOD_ID)
        // Register your block entities here
    }



}