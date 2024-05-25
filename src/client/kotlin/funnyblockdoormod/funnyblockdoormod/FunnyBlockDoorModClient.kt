package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screen.ModScreenHandlers
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screen.ingame.HandledScreens

object FunnyBlockDoorModClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		HandledScreens.register(ModScreenHandlers.DOOREMITTERSCREENHANDLER, ::DoorEmitterScreen)
	}
}