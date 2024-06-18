package funnyblockdoormod.funnyblockdoormod.datagen

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import java.util.concurrent.CompletableFuture

class ModBlockTagProvider(output: FabricDataOutput?,
                          registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>?
) : FabricTagProvider.BlockTagProvider(output, registriesFuture) {



    override fun configure(arg: RegistryWrapper.WrapperLookup?) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
            .add(ModBlocks.DOOREMITTER)
            .add(ModBlocks.REDSTONEEMITTER)
            .add(ModBlocks.REDSTONERECIEVER)
    }


}