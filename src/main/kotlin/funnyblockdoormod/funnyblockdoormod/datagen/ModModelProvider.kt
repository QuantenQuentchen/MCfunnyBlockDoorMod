package funnyblockdoormod.funnyblockdoormod.datagen

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.MOD_ID
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
                        Identifier(MOD_ID, "block/redstone_emitter")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier(MOD_ID, "block/redstone_emitter_active")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
            }
        })


        blockStateModelGenerator?.blockStateCollector?.accept(MultipartBlockStateSupplier
            .create(ModBlocks.DOOREMITTER).apply {
                Direction.Type.HORIZONTAL.forEach { direction ->
                    with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, false),
                        BlockStateVariant.create().put(
                            VariantSettings.MODEL,
                            Identifier(MOD_ID, "block/door_emitter")
                        ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                    with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                        BlockStateVariant.create().put(
                            VariantSettings.MODEL,
                            Identifier(MOD_ID, "block/door_emitter_active")
                        ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                }
            })

        //blockStateModelGenerator?.registerSimpleState(ModBlocks.DOOREMITTER)

        blockStateModelGenerator?.blockStateCollector?.accept(MultipartBlockStateSupplier
            .create(ModBlocks.REDSTONERECIEVER).apply {
                Direction.Type.HORIZONTAL.forEach { direction ->
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, false),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier(MOD_ID, "block/redstone_reciever")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                    BlockStateVariant.create().put(
                        VariantSettings.MODEL,
                        Identifier(MOD_ID, "block/redstone_reciever_active")
                    ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                }
            }
        )

/*        blockStateModelGenerator?.blockStateCollector?.accept(MultipartBlockStateSupplier
            .create(ModBlocks.FORCEFIELD).apply {
                Direction.Type.HORIZONTAL.forEach { direction ->
                    with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, false),
                        BlockStateVariant.create().put(
                            VariantSettings.MODEL,
                            Identifier(MOD_ID, "block/force_field_block")
                        ).put(VariantSettings.Y, VariantSettings.Rotation.entries[getYRotation(direction) / 90]))
                    with(When.create().set(Properties.HORIZONTAL_FACING, direction).set(Properties.POWERED, true),
                        BlockStateVariant.create().put(
                            VariantSettings.MODEL,
                            Identifier(MOD_ID, "block/force_field_block_active")
                        )
                            .put(VariantSettings.Y, VariantSettings.Rotation.entries[ge(direction) / 90])
                            .put(VariantSettings.X, VariantSettings.Rotation.entries[getXRotationOppositeFace(direction)])
                    )
                }
            }
        )*/

        blockStateModelGenerator?.blockStateCollector?.accept(
            VariantsBlockStateSupplier.create(ModBlocks.FORCEFIELD)
                .coordinate(
                    BlockStateVariantMap.create(Properties.FACING, Properties.POWERED)
                        .register { facing, powered ->
                            val (xRot, yRot) = getRotationForDirection(facing)
                            BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Identifier(MOD_ID,
                                    if (powered) "block/force_field_active" else "block/force_field"
                                    ))
                                .put(VariantSettings.Y, yRot)
                                .put(VariantSettings.X, xRot)
                    }
                )
        )
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {
        itemModelGenerator?.register(ModItems.UPGRADEBASE, Models.GENERATED)

    }

    private fun getRotationForDirection(direction: Direction): Pair<VariantSettings.Rotation?, VariantSettings.Rotation?> {
        return when (direction) {
            Direction.DOWN -> Pair(VariantSettings.Rotation.R180, VariantSettings.Rotation.R0)
            Direction.UP -> Pair(VariantSettings.Rotation.R0, VariantSettings.Rotation.R0)
            Direction.NORTH -> Pair(VariantSettings.Rotation.R90, VariantSettings.Rotation.R0)
            Direction.SOUTH -> Pair(VariantSettings.Rotation.R90, VariantSettings.Rotation.R180)
            Direction.EAST -> Pair(VariantSettings.Rotation.R90, VariantSettings.Rotation.R90)
            Direction.WEST -> Pair(VariantSettings.Rotation.R90, VariantSettings.Rotation.R270)
        }
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