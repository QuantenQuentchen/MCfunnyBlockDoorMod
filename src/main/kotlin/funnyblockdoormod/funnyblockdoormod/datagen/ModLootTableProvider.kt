package funnyblockdoormod.funnyblockdoormod.datagen

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableProvider
import net.minecraft.data.DataWriter
import net.minecraft.loot.LootTable
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class ModLootTableProvider(output: FabricDataOutput?) : FabricBlockLootTableProvider(output) {

    override fun generate() {
        addDrop(ModBlocks.DOOREMITTER)
        addDrop(ModBlocks.REDSTONEEMITTER)
        addDrop(ModBlocks.REDSTONERECIEVER)
    }

}