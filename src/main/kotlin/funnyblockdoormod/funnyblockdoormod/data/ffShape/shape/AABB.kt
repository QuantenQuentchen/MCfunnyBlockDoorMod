package funnyblockdoormod.funnyblockdoormod.data.ffShape.shape

import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import java.lang.Math.clamp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class AABB(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,

) : IShape {

    override val type = IShape.ShapeType.AABB

    override val centerI: BlockPos
        get() = BlockPos(
            ((minX + maxX) / 2.0).toInt(),
            ((minY + maxY) / 2.0).toInt(),
            ((minZ + maxZ) / 2.0).toInt()
        )

    override val centerD: Vec3d
        get() = Vec3d(
            (minX + maxX) /2.0,
            (minY + maxY) /2.0,
            (minZ + maxZ) /2.0
        )

    override val volume: Double
        get() {
            return (maxX - minX) * (maxY - minY) * (maxZ - minZ)
        }

    companion object: IShape.Factory {
        override fun deserializeFromNbt(tag: NbtCompound): IShape? {
            val minX = tag.getDouble("minX")
            val minY = tag.getDouble("minY")
            val minZ = tag.getDouble("minZ")
            val maxX = tag.getDouble("maxX")
            val maxY = tag.getDouble("maxY")
            val maxZ = tag.getDouble("maxZ")
            return AABB(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }

    override fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("minX", minX)
        tag.putDouble("minY", minY)
        tag.putDouble("minZ", minZ)
        tag.putDouble("maxX", maxX)
        tag.putDouble("maxY", maxY)
        tag.putDouble("maxZ", maxZ)
        return tag
    }

    constructor(minPos: BlockPos, maxPos: BlockPos) : this(
        minPos.x.toDouble(),
        minPos.y.toDouble(),
        minPos.z.toDouble(),
        maxPos.x.toDouble(),
        maxPos.y.toDouble(),
        maxPos.z.toDouble(),
    )

    override fun containsPoint(pos: BlockPos): Boolean {
        return pos.x >= minX && pos.x <= maxX &&
                pos.y >= minY && pos.y <= maxY &&
                pos.z >= minZ && pos.z <= maxZ
    }

    override fun getCoveredChunks(): List<ChunkPos> {
        val chunks = mutableListOf<ChunkPos>()

        // Convert block positions to chunk positions
        val minChunkX = (minX / 16.0).toInt()
        val minChunkZ = (minZ / 16.0).toInt()
        val maxChunkX = (maxX / 16.0).toInt()
        val maxChunkZ = (maxZ / 16.0).toInt()

        // Iterate through all chunks that intersect with the AABB
        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                chunks.add(ChunkPos(chunkX, chunkZ))
            }
        }

        return chunks
    }

    override fun getCoveredRegions(): List<RegionPos> {
        val regions = mutableListOf<RegionPos>()

        // A Minecraft region covers 512x512 blocks (32x32 chunks).
        val minRegionX = floor(minX / 512.0).toInt()
        val minRegionZ = floor(minZ / 512.0).toInt()
        val maxRegionX = floor(maxX / 512.0).toInt()
        val maxRegionZ = floor(maxZ / 512.0).toInt()

        for (rx in minRegionX..maxRegionX) {
            for (rz in minRegionZ..maxRegionZ) {
                regions.add(RegionPos(rx, rz))
            }
        }

        return regions
    }

    /**
     * Check if this AABB intersects with another AABB
     */
    fun directIntersection(other: AABB): Boolean {
        return this.minX <= other.maxX && this.maxX >= other.minX &&
                this.minY <= other.maxY && this.maxY >= other.minY &&
                this.minZ <= other.maxZ && this.maxZ >= other.minZ
    }

    override fun intersects(other: IShape): Boolean {
        return other.intersectsWith(this)
    }

    override fun intersectsWith(aabb: AABB): Boolean {
        return this.minX <= aabb.maxX && this.maxX >= aabb.minX &&
                this.minY <= aabb.maxY && this.maxY >= aabb.minY &&
                this.minZ <= aabb.maxZ && this.maxZ >= aabb.minZ
    }

    override fun intersectsWith(sphere: Sphere): Boolean {
        val closestX = sphere.centerX.coerceIn(this.minX, this.maxX)
        val closestY = sphere.centerY.coerceIn(this.minY, this.maxY)
        val closestZ = sphere.centerZ.coerceIn(this.minZ, this.maxZ)

        val dx = closestX - sphere.centerX
        val dy = closestY - sphere.centerY
        val dz = closestZ - sphere.centerZ
        val distanceSquared = dx * dx + dy * dy + dz * dz

        return distanceSquared <= sphere.radius * sphere.radius
    }

    override fun intersectionVolume(other: IShape): Double {
        return other.intersectionVolumeWith(this)
    }

    override fun intersectionVolumeWith(aabb: AABB): Double {
        if (!intersectsWith(aabb)) return 0.0

        val overlapMinX = maxOf(this.minX, aabb.minX)
        val overlapMinY = maxOf(this.minY, aabb.minY)
        val overlapMinZ = maxOf(this.minZ, aabb.minZ)
        val overlapMaxX = minOf(this.maxX, aabb.maxX)
        val overlapMaxY = minOf(this.maxY, aabb.maxY)
        val overlapMaxZ = minOf(this.maxZ, aabb.maxZ)

        return (overlapMaxX - overlapMinX) * (overlapMaxY - overlapMinY) * (overlapMaxZ - overlapMinZ)

    }

    override fun intersectionVolumeWith(sphere: Sphere): Double {
        if (!intersectsWith(sphere)) return 0.0

        // Monte Carlo approximation for sphere-box intersection
        val intersectBox = AABB(
            maxOf(this.minX, sphere.centerX - sphere.radius),
            maxOf(this.minY, sphere.centerY - sphere.radius),
            maxOf(this.minZ, sphere.centerZ - sphere.radius),
            minOf(this.maxX, sphere.centerX + sphere.radius),
            minOf(this.maxY, sphere.centerY + sphere.radius),
            minOf(this.maxZ, sphere.centerZ + sphere.radius),
        )

        val samples = 1000
        var insideCount = 0

        for (i in 0 until samples) {
            val x = intersectBox.minX + Math.random() * (intersectBox.maxX - intersectBox.minX)
            val y = intersectBox.minY + Math.random() * (intersectBox.maxY - intersectBox.minY)
            val z = intersectBox.minZ + Math.random() * (intersectBox.maxZ - intersectBox.minZ)

            val dx = x - sphere.centerX
            val dy = y - sphere.centerY
            val dz = z - sphere.centerZ

            if (dx * dx + dy * dy + dz * dz <= sphere.radius * sphere.radius) {
                insideCount++
            }
        }

        return intersectBox.volume * (insideCount.toDouble() / samples)
    }

    //This is cursed btw, like this is fucking stupid
    override fun computeOwnShrinkVector(other: IShape): Vec3d {
        return other.computeOwnShrinkVectorFlipped(this)
    }

    override fun computeOwnShrinkVectorFlipped(other: IShape): Vec3d {
        return other.computeOwnShrinkVectorWith(this)
    }

    override fun computeOwnShrinkVectorWith(aabb: AABB): Vec3d {
        val dx = min(this.maxX, aabb.maxX) - max(this.minX, aabb.minX)
        val dy = min(this.maxY, aabb.maxY) - max(this.minY, aabb.minY)
        val dz = min(this.maxZ, aabb.maxZ) - max(this.minZ, aabb.minZ)

        if (dx <= 0 || dy <= 0 || dz <= 0) return Vec3d.ZERO // no overlap

        // Pick minimal axis
        val absMin = minOf(dx, dy, dz)
        val center = centerI
        val opponentCenter = aabb.centerI
        val dir = when (absMin) {
            dx -> Vec3d(if (center.x < opponentCenter.x) -ceil(dx) else ceil(dx), 0.0, 0.0)
            dy -> Vec3d(0.0, if (center.y < opponentCenter.y) -ceil(dy) else ceil(dy), 0.0)
            else -> Vec3d(0.0, 0.0, if (center.z < opponentCenter.z) -ceil(dz) else ceil(dz))
        }

        return dir
    }

    override fun computeOwnShrinkVectorWith(sphere: Sphere): Vec3d {
        val closest = Vec3d(
            clamp(sphere.centerX, minX, maxX),
            clamp(sphere.centerY, minY, maxY),
            clamp(sphere.centerZ, minZ, maxZ)
        )
        val sphereCenter = Vec3d(sphere.centerX, sphere.centerY, sphere.centerZ)
        val delta = sphereCenter.subtract(closest)
        val dist = delta.length()
        if (dist >= sphere.radius) return Vec3d.ZERO

        val overlap = sphere.radius - dist
        val normal = if (dist == 0.0) Vec3d(1.0, 0.0, 0.0) else delta.normalize()
        val shrinkVec = normal.multiply(-ceil(overlap)) // negative = inward shrink

        return shrinkVec
    }


    override fun shrinkByVector(vector: Vec3d): AABB {
        return AABB(
            if (vector.x < 0) minX - vector.x else minX + vector.x,
            if (vector.y < 0) minY - vector.y else minY + vector.y,
            if (vector.z < 0) minZ - vector.z else minZ + vector.z,
            if (vector.x < 0) maxX + vector.x else maxX - vector.x,
            if (vector.y < 0) maxY + vector.y else maxY - vector.y,
            if (vector.z < 0) maxZ + vector.z else maxZ - vector.z,
        )
    }

    override fun getShrinkVectorFromBlockHit(pos: BlockPos): Vec3d {
        // Block center
        val px = pos.x + 0.5
        val py = pos.y + 0.5
        val pz = pos.z + 0.5

        // Distances to each face
        val dxMin = px - minX
        val dxMax = maxX - px
        val dyMin = py - minY
        val dyMax = maxY - py
        val dzMin = pz - minZ
        val dzMax = maxZ - pz

        // If block is outside AABB, no shrink
        if (dxMin < 0 || dxMax < 0 || dyMin < 0 || dyMax < 0 || dzMin < 0 || dzMax < 0) return Vec3d.ZERO

        // For each axis, pick the closest face
        val shrinkX = if (dxMin < dxMax) -dxMin else dxMax
        val shrinkY = if (dyMin < dyMax) -dyMin else dyMax
        val shrinkZ = if (dzMin < dzMax) -dzMin else dzMax

        // Pick axis with minimal absolute distance
        val absMin = minOf(kotlin.math.abs(shrinkX), kotlin.math.abs(shrinkY), kotlin.math.abs(shrinkZ))

        val vecX = if (absMin == kotlin.math.abs(shrinkX)) shrinkX else 0.0
        val vecY = if (absMin == kotlin.math.abs(shrinkY)) shrinkY else 0.0
        val vecZ = if (absMin == kotlin.math.abs(shrinkZ)) shrinkZ else 0.0

        // Discretize to blocks
        return Vec3d(
            if (vecX > 0) ceil(vecX) else -ceil(-vecX),
            if (vecY > 0) ceil(vecY) else -ceil(-vecY),
            if (vecZ > 0) ceil(vecZ) else -ceil(-vecZ)
        )
    }

    override fun shrinkVectorPercentage(vector: Vec3d): Double? {
        val centerX = (minX + maxX) / 2.0
        val centerY = (minY + maxY) / 2.0
        val centerZ = (minZ + maxZ) / 2.0

        // Calculate half-extents (distance from center to face)
        val halfExtentX = (maxX - minX) / 2.0
        val halfExtentY = (maxY - minY) / 2.0
        val halfExtentZ = (maxZ - minZ) / 2.0

        // For each axis, calculate how far the shrink goes toward center
        val percentages = mutableListOf<Double>()

        if (vector.x != 0.0) {
            val percentage = abs(vector.x) / halfExtentX
            if (percentage > 1.0) return null // Past center
            percentages.add(percentage)
        }

        if (vector.y != 0.0) {
            val percentage = abs(vector.y) / halfExtentY
            if (percentage > 1.0) return null
            percentages.add(percentage)
        }

        if (vector.z != 0.0) {
            val percentage = abs(vector.z) / halfExtentZ
            if (percentage > 1.0) return null
            percentages.add(percentage)
        }

        // Return the maximum percentage across all axes
        return percentages.maxOrNull() ?: 0.0
    }

    /**
     * Get the volume of this AABB
     */

    /**
     * Expand this AABB by the given amount in all directions
     */
    fun expand(amount: Double): AABB {
        return AABB(
            minX - amount, minY - amount, minZ - amount,
            maxX + amount, maxY + amount, maxZ + amount,
        )
    }

    override fun toString(): String {
        return "AABB(min=[$minX, $minY, $minZ], max=[$maxX, $maxY, $maxZ])"
    }

    override fun toUIString(): String {
        return "Cube([$minX, $minY, $minZ], [$maxX, $maxY, $maxZ])"
    }

    override fun getBoundingBox(): AABB {
        return this
    }

    fun union(other: AABB) = AABB(
        minOf(this.minX, other.minX), minOf(this.minY, other.minY), minOf(this.minZ, other.minZ),
        maxOf(this.maxX, other.maxX), maxOf(this.maxY, other.maxY), maxOf(this.maxZ, other.maxZ),
    )

}