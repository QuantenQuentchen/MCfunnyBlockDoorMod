package funnyblockdoormod.funnyblockdoormod.render.shape

import net.minecraft.client.gl.VertexBuffer

data class VBOCacheEntry(
    val vbo: VertexBuffer,
    val faceCount: Int,
    val vertexCount: Int
)