package funnyblockdoormod.funnyblockdoormod.item

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.MOD_ID
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModItems {

    val UPGRADEBASE: Item = registerItem("upgrade_base", Item(FabricItemSettings()))

    private fun addItemToItemGroup(item: Item, group: FabricItemGroupEntries) {
        FunnyBlockDoorMod.logger.info("Adding item $item to item group $group")
        group.add(item)
    }

    private fun registerItem(name: String, item: Item): Item {
        FunnyBlockDoorMod.logger.info("Registering item $name for $MOD_ID")
        return Registry.register(Registries.ITEM, Identifier(MOD_ID, name), item)
    }

    fun registerItems() {
        FunnyBlockDoorMod.logger.info("Registering items for $MOD_ID")



        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { group ->
            addItemToItemGroup(UPGRADEBASE, group)
        }
    }

}