package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.datagen.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object FunnyBlockDoorModDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		val pack = fabricDataGenerator.createPack()

		pack.addProvider(::ModBlockTagProvider)
		pack.addProvider(::ModItemTagProvider)
		pack.addProvider(::ModLootTableProvider)
		pack.addProvider(::ModModelProvider)
		pack.addProvider(::ModRecipeProvider)

	}
}