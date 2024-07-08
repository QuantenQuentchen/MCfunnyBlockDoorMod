package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import funnyblockdoormod.funnyblockdoormod.core.containerClasses.imgPartInfo
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.IntPoint2D
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

data class ButtonIcon(val identifier: Identifier, val u: Int,
                      val v: Int, val textureWidth: Int,
                      val textureHeight: Int){

    val clickMap: Set<IntPoint2D> by lazy { genClickMap() }

    companion object {
        private val resourceManager: ResourceManager by lazy {
            MinecraftClient.getInstance().resourceManager
        }
        private val ButtonIconCache: MutableMap<ButtonIconKey, ButtonIcon> = mutableMapOf()

        fun getButtonIcon(identifier: Identifier, u: Int, v: Int, textureWidth: Int, textureHeight: Int): ButtonIcon {
            val key = ButtonIconKey(identifier, u, v, textureWidth, textureHeight)
            return ButtonIconCache.getOrPut(key) {
                ButtonIcon(key.identifier, key.u, key.v, key.textureWidth, key.textureHeight)
            }
        }

    }

    private fun genClickMap(): Set<IntPoint2D> {
        val resource = resourceManager.getResource(identifier).get()
        val nativeImage = NativeImage.read(resource.inputStream)
        val clickMap = mutableSetOf<IntPoint2D>()
        for (i in 0 until textureWidth) {
            for (j in 0 until textureHeight) {
                if(nativeImage.getOpacity(u + i, v + j).toInt() == 0) continue
                clickMap.add(IntPoint2D(i, j))
            }
        }
        nativeImage.close()
        resource.inputStream.close()
        return clickMap
    }

}
