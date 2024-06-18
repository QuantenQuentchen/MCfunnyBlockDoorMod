package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screen.ModScreenHandlers
import funnyblockdoormod.funnyblockdoormod.screen.WirelessRedstoneScreen
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens

object FunnyBlockDoorModClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		HandledScreens.register(ModScreenHandlers.DOOREMITTERSCREENHANDLER, ::DoorEmitterScreen)

		HandledScreens.register(ModScreenHandlers.WIRELESS_REDSTONE_SCREEN_HANDLER, ::WirelessRedstoneScreen)

	}
}