package funnyblockdoormod.funnyblockdoormod.datagen

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import funnyblockdoormod.funnyblockdoormod.item.ModItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.*
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

class ModModelProvider(output: FabricDataOutput?) : FabricModelProvider(output) {

    override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator?) {

        blockStateModelGenerator?.blockStateCollector?.accept(MultipartBlockStateSupplier
            .create(ModBlocks.REDSTONEEMITTER).apply {
            Direction.Type.HORIZONTAL.forEach { direction ->
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, false),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier("funnyblockdoormod", "block/redstone_emitter")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier("funnyblockdoormod", "block/redstone_emitter_active")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
            }
        })


        blockStateModelGenerator?.registerSimpleState(ModBlocks.DOOREMITTER)


        blockStateModelGenerator?.blockStateCollector?.accept(MultipartBlockStateSupplier
            .create(ModBlocks.REDSTONERECIEVER).apply {
                Direction.Type.HORIZONTAL.forEach { direction ->
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, false),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier("funnyblockdoormod", "block/redstone_reciever")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier("funnyblockdoormod", "block/redstone_reciever_active")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
            }
        })

    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {
        itemModelGenerator?.register(ModItems.UPGRADEBASE, Models.GENERATED)

    }

    private fun getYRotation(direction: Direction): Int {
        return when (direction) {
            Direction.NORTH -> 0
            Direction.EAST -> 90
            Direction.SOUTH -> 180
            Direction.WEST -> 270
            else -> 0
        }
    }

}