package funnyblockdoormod.funnyblockdoormod.render

import CubeCache
import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.Vec3f
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.Vec4f
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.minus
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.AABB
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.Sphere
import funnyblockdoormod.funnyblockdoormod.extensions.translate
import funnyblockdoormod.funnyblockdoormod.render.shape.SphereCache
import funnyblockdoormod.funnyblockdoormod.render.shape.VBOCacheEntry
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import java.util.UUID
import kotlin.compareTo

object Renderer {

    private const val POLY_OFFSET_FACTOR = -1.0f
    private const val POLY_OFFSET_UNITS  = -2.0f

    val drawList = mutableListOf<FFComponent>()

    val lookupTable = mutableMapOf<UUID, FFComponent>()

    fun register(){
        WorldRenderEvents.LAST.register { ctx ->
            draw(ctx)
        }
    }

    fun clear() {
        drawList.clear()
        lookupTable.clear()
    }

    fun addShape(shape: FFComponent) {
        val uuid = shape.ownership?.uuid ?: return
        drawList.add(shape)
        lookupTable[uuid] = shape
    }

    fun modifyShape(shape: FFComponent) {
        val uuid = shape.ownership?.uuid ?: return
        lookupTable[uuid] = shape
    }

    fun removeShape(shape: FFComponent) {
        drawList.remove(shape)
    }

    fun removeShape(uuid: UUID) {
        val shape = lookupTable[uuid] ?: return
        lookupTable.remove(uuid)
        drawList.remove(shape)
    }

    private fun draw(ctx: WorldRenderContext){
        for (obj in drawList) {
            when (obj.shape) {
                is Sphere -> draw(ctx, obj.shape as Sphere)
                is AABB -> draw(ctx, obj.shape as AABB)
            }
        }
    }

    private fun draw(ctx: WorldRenderContext, obj: Sphere){
        val vbo = SphereCache.getOrCreate(obj.radius.toInt())
        draw(ctx, vbo, obj.centerD, Vec4f(1f, 1f, 1f, .5f))
    }

    private fun draw(ctx: WorldRenderContext, obj: AABB){
        val vbo = CubeCache.getUnitCube()
        val size = Vec3f(
            (obj.maxX - obj.minX).toFloat(),//*.5f,
            (obj.maxY - obj.minY).toFloat(),//*.5f,
            (obj.maxZ - obj.minZ).toFloat(),//*.5f
        )
        draw(ctx, vbo, obj.centerD, Vec4f(1f, 1f, 1f, .5f), size)
    }


    private fun draw(
        ctx: WorldRenderContext,
        vbo: VBOCacheEntry,
        center: Vec3d,
        color: Vec4f,
        size: Vec3f = Vec3f(1.0f, 1.0f, 1.0f),
        rotation: Quaternionf? = null,
        disableDepthTest: Boolean = false
    ){
        val cameraPos = ctx.camera().pos ?: return
        val matrices = ctx.matrixStack() ?: return

        // Keep depth testing by default
        val useDepthBias = !disableDepthTest
        if (disableDepthTest) RenderSystem.disableDepthTest()

        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.setShaderColor(color.x, color.y, color.z, color.w)

        if (color.w < 1.0f){
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.depthMask(false)
        }

        // Apply small polygon offset to avoid z-fighting
        if (useDepthBias) {
            RenderSystem.enablePolygonOffset()
            RenderSystem.polygonOffset(POLY_OFFSET_FACTOR, POLY_OFFSET_UNITS)
        }

        matrices.push()
        matrices.translate(center - cameraPos)
        if (rotation != null) matrices.multiply(rotation)
        matrices.scale(size.x, size.y, size.z)

        vbo.vbo.bind()
        vbo.vbo.draw(
            matrices.peek().positionMatrix,
            RenderSystem.getProjectionMatrix(),
            RenderSystem.getShader()
        )
        VertexBuffer.unbind()
        matrices.pop()

        // Reset polygon offset state
        if (useDepthBias) {
            RenderSystem.polygonOffset(0f, 0f)
            RenderSystem.disablePolygonOffset()
        }

        if (color.w < 1.0f){
            RenderSystem.disableBlend()
            RenderSystem.depthMask(true)
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

        if (disableDepthTest) RenderSystem.enableDepthTest()
    }

}