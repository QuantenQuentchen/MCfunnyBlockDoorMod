package funnyblockdoormod.funnyblockdoormod.item

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ModItemGroups {

    val modItemGroup: ItemGroup = Registry.register(Registries.ITEM_GROUP,
        Identifier(FunnyBlockDoorMod.MOD_ID, "funny_block_doors"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.funnyblockdoormod"))
            .icon { ItemStack(ModItems.UPGRADEBASE) }
            .entries { _, entries ->
                entries.add(ModItems.UPGRADEBASE)
                entries.add(ModBlocks.DOOREMITTER)
            }
            .build())

    fun registerItemGroups() {
        FunnyBlockDoorMod.logger.info("Registering item groups for " + FunnyBlockDoorMod.MOD_ID)

    }


}