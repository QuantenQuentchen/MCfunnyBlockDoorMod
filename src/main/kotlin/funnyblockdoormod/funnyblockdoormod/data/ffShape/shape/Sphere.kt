package funnyblockdoormod.funnyblockdoormod.data.ffShape.shape

//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.lang.Math.clamp
import kotlin.math.*

class Sphere(
    val centerX: Double,
    val centerY: Double,
    val centerZ: Double,
    val radius: Double

) : IShape {

    override val type = IShape.ShapeType.SPHERE

    override val centerI: BlockPos
        get() = BlockPos(centerX.toInt(), centerY.toInt(), centerZ.toInt())

    override val centerD: Vec3d
        get() = Vec3d(centerX, centerY, centerZ)

    override val volume: Double
        get() = (4.0 / 3.0) * PI * radius * radius * radius


    companion object : IShape.Factory {
        override fun deserializeFromNbt(tag: NbtCompound): IShape? {
            val centerX = tag.getDouble("centerX")
            val centerY = tag.getDouble("centerY")
            val centerZ = tag.getDouble("centerZ")
            val radius = tag.getDouble("radius")
            return Sphere(centerX, centerY, centerZ, radius)
        }
    }

    override fun containsPoint(pos: BlockPos): Boolean {
        val dx = pos.x - centerX
        val dy = pos.y - centerY
        val dz = pos.z - centerZ
        val distanceSquared = dx * dx + dy * dy + dz * dz
        return distanceSquared <= radius * radius
    }

    fun isOnBorder(pos: BlockPos): Boolean {

        if (!containsPoint(pos)) return false

        // Check if any of the 6 neighboring blocks are outside the sphere
        val neighbors = arrayOf(
            BlockPos(pos.x + 1, pos.y, pos.z),
            BlockPos(pos.x - 1, pos.y, pos.z),
            BlockPos(pos.x, pos.y + 1, pos.z),
            BlockPos(pos.x, pos.y - 1, pos.z),
            BlockPos(pos.x, pos.y, pos.z + 1),
            BlockPos(pos.x, pos.y, pos.z - 1)
        )

        return neighbors.any { neighbor -> !containsPoint(neighbor) }
    }

    override fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("centerX", centerX)
        tag.putDouble("centerY", centerY)
        tag.putDouble("centerZ", centerZ)
        tag.putDouble("radius", radius)
        return tag
    }

    override fun getCoveredChunks(): List<ChunkPos> {
        val chunks = mutableListOf<ChunkPos>()

        val minChunkX = ((centerX - radius) / 16.0).toInt()
        val minChunkZ = ((centerZ - radius) / 16.0).toInt()
        val maxChunkX = ((centerX + radius) / 16.0).toInt()
        val maxChunkZ = ((centerZ + radius) / 16.0).toInt()

        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                chunks.add(ChunkPos(chunkX, chunkZ))
            }
        }

        return chunks
    }

    override fun getCoveredRegions(): List<RegionPos> {
        val regions = mutableListOf<RegionPos>()

        val minRegionX = floor((centerX - radius) / 512.0).toInt()
        val minRegionZ = floor((centerZ - radius) / 512.0).toInt()
        val maxRegionX = floor((centerX + radius) / 512.0).toInt()
        val maxRegionZ = floor((centerZ + radius) / 512.0).toInt()

        for (rx in minRegionX..maxRegionX) {
            for (rz in minRegionZ..maxRegionZ) {
                regions.add(RegionPos(rx, rz))
            }
        }

        return regions
    }

/*    override fun renderDebugShape(context: WorldRenderContext) {
        // Render bounding box for now - proper sphere rendering is more complex
        val bbox = getBoundingBox()
        bbox.renderDebugShape(context)
    }
*/

    override fun getBoundingBox(): AABB {
        return AABB(
            centerX - radius,
            centerY - radius,
            centerZ - radius,
            centerX + radius,
            centerY + radius,
            centerZ + radius,
        )
    }

    override fun intersects(other: IShape): Boolean {
        return other.intersectsWith(this)
    }

    override fun intersectsWith(aabb: AABB): Boolean {
        return aabb.intersectsWith(this)
    }

    override fun intersectsWith(sphere: Sphere): Boolean {
        val dx = this.centerX - sphere.centerX
        val dy = this.centerY - sphere.centerY
        val dz = this.centerZ - sphere.centerZ
        val distanceSquared = dx * dx + dy * dy + dz * dz
        val radiusSum = this.radius + sphere.radius
        return distanceSquared <= radiusSum * radiusSum
    }

    override fun intersectionVolume(other: IShape): Double {
        return other.intersectionVolume(this)
    }

    override fun intersectionVolumeWith(aabb: AABB): Double {
        return aabb.intersectionVolumeWith(this)
    }

    override fun intersectionVolumeWith(sphere: Sphere): Double {
        val dx = this.centerX - sphere.centerX
        val dy = this.centerY - sphere.centerY
        val dz = this.centerZ - sphere.centerZ
        val d = sqrt(dx * dx + dy * dy + dz * dz)

        val r1 = this.radius
        val r2 = sphere.radius

        if (d >= r1 + r2) return 0.0

        if (d <= abs(r1 - r2)) {
            val smallerRadius = min(r1, r2)
            return (4.0 / 3.0) * PI * smallerRadius * smallerRadius * smallerRadius
        }

        val part1 = (r1 + r2 - d) * (r1 + r2 - d) *
                (d * d + 2.0 * d * r2 - 3.0 * r2 * r2 + 2.0 * d * r1 + 6.0 * r2 * r1 - 3.0 * r1 * r1)
        return PI * part1 / (12.0 * d)
    }

    override fun computeOwnShrinkVector(other: IShape): Vec3d {
        return other.computeOwnShrinkVectorFlipped(this)
    }

    override fun computeOwnShrinkVectorFlipped(other: IShape): Vec3d {
        return other.computeOwnShrinkVectorWith(this)
    }

    override fun computeOwnShrinkVectorWith(aabb: AABB): Vec3d {
        // Find closest point on the box to the sphere center
        val closest = Vec3d(
            clamp(centerX, aabb.minX, aabb.maxX),
            clamp(centerY, aabb.minY, aabb.maxY),
            clamp(centerZ, aabb.minZ, aabb.maxZ)
        )

        // Vector from sphere center to closest point on box
        val delta = Vec3d(centerX - closest.x, centerY - closest.y, centerZ - closest.z)
        val dist = delta.length()

        // No overlap
        if (dist >= radius) return Vec3d.ZERO

        // Overlap amount
        val overlap = radius - dist

        // Normal direction: inward shrink
        val normal = if (dist == 0.0) Vec3d(1.0, 0.0, 0.0) else delta.normalize()

        // Shrink vector points inward along normal
        return normal.multiply(-ceil(overlap))
    }

    override fun computeOwnShrinkVectorWith(sphere: Sphere): Vec3d {
        // Vector from this sphere to the other sphere
        val delta = Vec3d(sphere.centerX - this.centerX, sphere.centerY - this.centerY, sphere.centerZ - this.centerZ)
        val dist = delta.length()

        // No overlap
        if (dist >= this.radius + sphere.radius) return Vec3d.ZERO

        // Overlap amount: how much this sphere should shrink
        val overlap = (this.radius + sphere.radius) - dist

        // Direction for shrinking this sphere
        val normal = if (dist == 0.0) Vec3d(1.0, 0.0, 0.0) else delta.normalize()

        // Shrink inward along the normal
        return normal.multiply(-ceil(overlap))

    }


    override fun shrinkByVector(vector: Vec3d): Sphere? {
        // Shrink the radius by the magnitude of the vector
        val shrinkAmount = vector.length()
        val newRadius = (radius - shrinkAmount)
        if (newRadius <= 0.0) return null
        return Sphere(centerX, centerY, centerZ, newRadius)
    }

    override fun getShrinkVectorFromBlockHit(blockPos: BlockPos): Vec3d {
        // Block center
        val P = Vec3d(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)

        // Vector from sphere center to block
        val delta = P.subtract(Vec3d(centerX, centerY, centerZ))
        val dist = delta.length()

        // Block is outside or exactly at center
        if (dist >= radius) return Vec3d.ZERO
        if (dist == 0.0) return Vec3d(-1.0, 0.0, 0.0)  // arbitrary negative axis

        // Closest point on surface along the direction to the block
        val surfacePoint = Vec3d(centerX, centerY, centerZ).add(delta.normalize().multiply(radius))

        // Shrink vector from surface to block
        val shrinkVec = P.subtract(surfacePoint)

        // Discretize to blocks
        return Vec3d(
            if (shrinkVec.x > 0) ceil(shrinkVec.x) else -ceil(-shrinkVec.x),
            if (shrinkVec.y > 0) ceil(shrinkVec.y) else -ceil(-shrinkVec.y),
            if (shrinkVec.z > 0) ceil(shrinkVec.z) else -ceil(-shrinkVec.z)
        )
    }

    override fun shrinkVectorPercentage(vector: Vec3d): Double? {
        val shrinkDistance = vector.length()

        if (shrinkDistance == 0.0) return 0.0

        // How far is the shrink compared to the radius?
        val percentage = shrinkDistance / radius

        if (percentage > 1.0) return null // Would collapse past center

        return percentage
    }


    override fun toString(): String {
        return "Sphere(center=[$centerX, $centerY, $centerZ], radius=$radius)"
    }

    override fun toUIString(): String {
        return "Sphere([$centerX, $centerY, $centerZ], $radius)"
    }

    constructor(center: BlockPos, radius: Double) : this(center.x.toDouble(), center.y.toDouble(), center.z.toDouble(), radius)
    constructor(center: Vec3d, radius: Double) : this(center.x, center.y, center.z, radius)

}