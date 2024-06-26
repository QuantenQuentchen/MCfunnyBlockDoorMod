package funnyblockdoormod.funnyblockdoormod.block

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.custom.doorEmitterBlock
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModBlocks {

    val DOOREMITTER: Block = registerBlock("door_emitter",
        doorEmitterBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK).nonOpaque()))

    val REDSTONEEMITTER: Block = registerBlock("redstone_emitter",
        RedstoneEmitter(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK).nonOpaque()))

    val REDSTONERECIEVER: Block = registerBlock("redstone_reciever",
        RedstoneReciever(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK).nonOpaque()))

    private fun registerBlockItem(name: String, block: Block): Item {
        return Registry.register(Registries.ITEM, Identifier(FunnyBlockDoorMod.MOD_ID, name),
            BlockItem(block, FabricItemSettings()))
    }

    private fun registerBlock(name: String, block: Block): Block {
        registerBlockItem(name, block)
        return Registry.register(Registries.BLOCK, Identifier(FunnyBlockDoorMod.MOD_ID, name), block)
    }

    fun registerModBlocks() {
        FunnyBlockDoorMod.logger.info("Registering blocks for " + FunnyBlockDoorMod.MOD_ID)

    }

}