package funnyblockdoormod.funnyblockdoormod.datagen

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.util.Identifier
import java.util.function.Consumer

class ModRecipeProvider(output: FabricDataOutput?) : FabricRecipeProvider(output) {

    //TODO: Add wirelessRedstoneEmitter and wirelessRedstoneReciever recipes

    override fun generate(exporter: Consumer<RecipeJsonProvider>?) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.DOOREMITTER.asItem(),1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .input('#', Items.ENDERMAN_SPAWN_EGG)
            .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
            .offerTo(exporter, Identifier(getRecipeName(ModBlocks.DOOREMITTER.asItem())))
    }
}