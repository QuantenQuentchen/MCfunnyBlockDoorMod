package funnyblockdoormod.funnyblockdoormod.render.shape

import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.Sphere
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

object SphereCache {

    private val cache = mutableMapOf<Int, VBOCacheEntry>()

    fun getOrCreate(radius: Int): VBOCacheEntry {
        return cache.getOrPut(radius) { buildSphereVBO(radius) }
    }

    private fun buildSphereVBO(radius: Int): VBOCacheEntry {
        val offsets = generateBorderOffsets(radius)
        val vbo = VertexBuffer(VertexBuffer.Usage.STATIC)

        val builder = Tessellator.getInstance().buffer
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        var vertexCount = 0
        var faceCount = 0

        val testSphere = Sphere(0.0, 0.0, 0.0, radius.toDouble())

        for (offset in offsets) {
            val x = offset.x
            val y = offset.y
            val z = offset.z
            val result = addOutsideFaces(builder, x, y, z, testSphere)
            vertexCount += result.first
            faceCount += result.second
        }

        val builtBuffer = builder.end()
        vbo.bind()
        vbo.upload(builtBuffer)
        VertexBuffer.unbind()
        return VBOCacheEntry(vbo, vertexCount, faceCount)
    }

    private fun addOutsideFaces(
        builder: BufferBuilder,
        x: Int, y: Int, z: Int,
        sphere: Sphere
    ): Pair<Int, Int> {
        val fx = x.toDouble()
        val fy = y.toDouble()
        val fz = z.toDouble()
        val size = 1f
        var vertexCount = 0
        var faceCount = 0

        // Bottom (-Y) - render if neighbor below is NOT inside sphere (facing outward)
        if (!sphere.containsPoint(BlockPos(x, y - 1, z))) {
            // Outside face
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        // Top (+Y)
        if (!sphere.containsPoint(BlockPos(x, y + 1, z))) {
            // Outside face
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        // North (-Z)
        if (!sphere.containsPoint(BlockPos(x, y, z - 1))) {
            // Outside face
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        // South (+Z)
        if (!sphere.containsPoint(BlockPos(x, y, z + 1))) {
            // Outside face
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        // West (-X)
        if (!sphere.containsPoint(BlockPos(x - 1, y, z))) {
            // Outside face
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx, fy, fz).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        // East (+X)
        if (!sphere.containsPoint(BlockPos(x + 1, y, z))) {
            // Outside face
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            // Inside face (reversed winding)
            builder.vertex(fx + size, fy, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz + size).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy + size, fz).color(1f, 1f, 1f, 1f).next()
            builder.vertex(fx + size, fy, fz).color(1f, 1f, 1f, 1f).next()
            vertexCount += 8
            faceCount += 2
        }

        return Pair(vertexCount, faceCount)
    }

    private fun generateBorderOffsets(radius: Int): List<Vec3i> {
        if (radius == 0) return listOf(Vec3i(0, 0, 0))

        val rSqThreshold = (radius + 0.5).let { it * it }
        val borderList = ArrayList<Vec3i>()
        val testSphere = Sphere(0.0, 0.0, 0.0, radius.toDouble())

        for (x in -radius..radius) {
            val x2 = x * x
            for (y in -radius..radius) {
                val xy2 = x2 + y * y
                if (xy2 > rSqThreshold) continue

                for (z in -radius..radius) {
                    if (testSphere.isOnBorder(BlockPos(x, y, z))) {
                        borderList.add(Vec3i(x, y, z))
                    }
                }
            }
        }

        return borderList
    }
}