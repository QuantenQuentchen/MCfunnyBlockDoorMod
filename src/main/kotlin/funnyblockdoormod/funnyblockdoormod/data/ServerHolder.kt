package funnyblockdoormod.funnyblockdoormod.data

import net.minecraft.server.MinecraftServer

object ServerHolder {
    @Volatile
    var server: MinecraftServer? = null
}