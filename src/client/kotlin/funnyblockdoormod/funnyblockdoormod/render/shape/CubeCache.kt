import funnyblockdoormod.funnyblockdoormod.render.shape.VBOCacheEntry
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

object CubeCache {

    private var cached: VBOCacheEntry? = null

    fun getUnitCube(): VBOCacheEntry {
        return cached ?: buildUnitCube().also { cached = it }
    }

    private fun buildUnitCube(): VBOCacheEntry {
        val builder = Tessellator.getInstance().buffer
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        val size = 1.0f
        val half = size / 2.0f

        // Cube corners (centered at origin)
        val corners = arrayOf(
            floatArrayOf(-half, -half, -half),
            floatArrayOf( half, -half, -half),
            floatArrayOf( half, -half,  half),
            floatArrayOf(-half, -half,  half),
            floatArrayOf(-half,  half, -half),
            floatArrayOf( half,  half, -half),
            floatArrayOf( half,  half,  half),
            floatArrayOf(-half,  half,  half)
        )

        // Each face as quad indices (4 vertices)
        // Outside faces (counter-clockwise when viewed from outside)
        val Faces = arrayOf(
            intArrayOf(0, 1, 2, 3), // Bottom
            intArrayOf(4, 7, 6, 5), // Top
            intArrayOf(0, 3, 7, 4), // West (-X)
            intArrayOf(1, 5, 6, 2), // East (+X)
            intArrayOf(0, 4, 5, 1), // North (-Z)
            intArrayOf(3, 2, 6, 7),  // South (+Z)

        // Inside faces (clockwise when viewed from outside = counter-clockwise from inside)
            intArrayOf(0, 3, 2, 1), // Bottom (reversed)
            intArrayOf(4, 5, 6, 7), // Top (reversed)
            intArrayOf(0, 4, 7, 3), // West (reversed)
            intArrayOf(1, 2, 6, 5), // East (reversed)
            intArrayOf(0, 1, 5, 4), // North (reversed)
            intArrayOf(3, 7, 6, 2)  // South (reversed)
        )

        var vertexCount = 0

        // Render outside faces
        for (face in Faces) {
            for (i in face) {
                val v = corners[i]
                builder.vertex(v[0].toDouble(), v[1].toDouble(), v[2].toDouble())
                    .color(1f, 1f, 1f, 1f)
                    .next()
            }
            vertexCount += 4
        }

        // Render inside faces
/*        for (face in insideFaces) {
            for (i in face) {
                val v = corners[i]
                builder.vertex(v[0].toDouble(), v[1].toDouble(), v[2].toDouble())
                    .color(1f, 1f, 1f, 1f)
                    .next()
            }
            vertexCount += 4
        }*/

        val vbo = VertexBuffer(VertexBuffer.Usage.STATIC)
        vbo.bind()
        vbo.upload(builder.end())
        VertexBuffer.unbind()

        return VBOCacheEntry(vbo, 12, vertexCount) // 12 faces now (6 outside + 6 inside)
    }
}