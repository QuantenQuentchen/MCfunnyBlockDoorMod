package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import net.minecraft.util.Identifier

data class ButtonIcon(val identifier: Identifier, val u: Int,
                      val v: Int, val textureWidth: Int,
                      val textureHeight: Int, val hoveredVOffset: Int,
                      val displayWidth: Int, val displayHeight: Int)
