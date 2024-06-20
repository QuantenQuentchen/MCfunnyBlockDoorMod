package funnyblockdoormod.funnyblockdoormod.annotations

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

object DivisionUtil {

    fun assertClientOnly() {
        if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            FunnyBlockDoorMod.logger.error("This method can only be called on the client side!")
            throw IllegalStateException("This method can only be called on the client side!")
        }
    }

    fun assertServerOnly() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            FunnyBlockDoorMod.logger.error("This method can only be called on the server side!")
            throw IllegalStateException("This method can only be called on the server side!")
        }
    }

}