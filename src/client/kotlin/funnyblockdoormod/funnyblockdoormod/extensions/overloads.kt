package funnyblockdoormod.funnyblockdoormod.extensions

import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.Vec4d
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.Vec4f
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

fun MatrixStack.translate(translation: Vec3d) = this.translate(translation.x, translation.y, translation.z)
//fun RenderSystem.setShaderColor(color: Vec4f): Unit = this.setShaderColor(color.x, color.y, color.z, color.w)