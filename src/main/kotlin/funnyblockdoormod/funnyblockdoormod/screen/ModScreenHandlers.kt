package funnyblockdoormod.funnyblockdoormod.screen

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


object ModScreenHandlers {
    val DOOREMITTERSCREENHANDLER =
        Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier(FunnyBlockDoorMod.MOD_ID, "door_emitter"),
            ExtendedScreenHandlerType(::DoorEmitterScreenHandler)
        )

    fun registerScreenHandlers() {
        FunnyBlockDoorMod.logger.info("Registering screen handlers for " + FunnyBlockDoorMod.MOD_ID)
    }
}