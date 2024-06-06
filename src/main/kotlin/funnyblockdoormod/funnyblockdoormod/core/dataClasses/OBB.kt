package funnyblockdoormod.funnyblockdoormod.core.dataClasses


import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockPos3DGrid
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


data class OBB(
    val center: Point3D,
    val u: Vec3d, // First basis vector
    val v: Vec3d, // Second basis vector
    val w: Vec3d, // Third basis vector
    val extents: Vec3d, // Half-dimensions along each axis
) {

    companion object {
        const val MAX_OBB_SIZE = 25
        const val SIZE_U = 5.0
        const val SIZE_V = 5.0
        const val LENGTH = 25.0
        private val uVec = Vec3d(1.0, 0.0, 0.0)
        private val vVec = Vec3d(0.0, 1.0, 0.0)
        private val wVec = Vec3d(0.0, 0.0, 1.0)
        private val center = Point3D(0.0, 0.0, LENGTH / 2)
        private val extents = Vec3d(SIZE_U / 2, SIZE_V / 2, LENGTH / 2)
        private val obbMap = WeakHashMap<Int, OBB>()

        private val emittingGridMap = WeakHashMap<Int, BlockPos3DGrid>()

        private fun rotate(angleX: Float, angleY: Float, angleZ: Float, vec: Vec3d): Vec3d {
            // Convert angles to radians
            val radX = Math.toRadians(angleX.toDouble())
            val radY = Math.toRadians(angleY.toDouble())
            val radZ = Math.toRadians(angleZ.toDouble())

            // Calculate cosine and sine of angles
            val cosX = cos(radX)
            val sinX = sin(radX)
            val cosY = cos(radY)
            val sinY = sin(radY)
            val cosZ = cos(radZ)
            val sinZ = sin(radZ)

            // Apply rotation around X-axis
            val newX1 = vec.x
            val newY1 = vec.y * cosX - vec.z * sinX
            val newZ1 = vec.y * sinX + vec.z * cosX

            // Apply rotation around Y-axis
            val newX2 = newX1 * cosY + newZ1 * sinY
            val newY2 = newY1
            val newZ2 = -newX1 * sinY + newZ1 * cosY

            // Apply rotation around Z-axis
            val newX3 = newX2 * cosZ - newY2 * sinZ
            val newY3 = newX2 * sinZ + newY2 * cosZ
            val newZ3 = newZ2

            return Vec3d(newX3, newY3, newZ3)
        }

        fun getRotatedOBB(angleX: Float, angleY: Float, angleZ: Float): OBB {
            val rotationCompInt = encodeAngles(angleX.toInt(), angleY.toInt(), angleZ.toInt())
            obbMap[rotationCompInt]?.let { return it }

            val rotatedCenter = rotate(angleX, angleY, angleZ, center.toVec3d())

            val rotatedU = rotate(angleX, angleY, angleZ, uVec)
            val rotatedV = rotate(angleX, angleY, angleZ, vVec)
            val rotatedW = rotate(angleX, angleY, angleZ, wVec)

            val rotatedObb = OBB(Point3D(rotatedCenter), rotatedU, rotatedV, rotatedW, extents)

            obbMap[rotationCompInt] = rotatedObb
            return rotatedObb
        }

/*        private fun getRotatedOBBp(angleX: Float, angleY: Float, angleZ: Float): OBB {
            val rotationCompInt = encodeAngles(angleX.toInt(), angleY.toInt(), angleZ.toInt())
            obbMap[rotationCompInt]?.let { return it }

            // Rotate the basis vectors
            val newU = rotate(angleX, angleY, angleZ, uVec)
            val newV = rotate(angleX, angleY, angleZ, vVec)
            val newW = rotate(angleX, angleY, angleZ, wVec)

            // The center point is always at the origin
            val newCenter = Point3D(0.0, 0.0, 0.0)

            val rotatedObb = OBB(newCenter, newU, newV, newW, extents)
            obbMap[rotationCompInt] = rotatedObb
            return rotatedObb
        }*/

        fun getEmittingGrid(angleX: Float, angleY: Float, angleZ: Float): BlockPos3DGrid {
            val rotationCompInt = encodeAngles(angleX.toInt(), angleY.toInt(), angleZ.toInt())
            emittingGridMap[rotationCompInt]?.let { return it }
            val obb = getRotatedOBB(angleX, angleY, angleZ)
            val grid = obb.voxelize()
            emittingGridMap[rotationCompInt] = grid
            return grid
        }

        private fun encodeAngles(angleX: Int, angleY: Int, angleZ: Int): Int {
            return (angleX shl 16) or (angleY shl 8) or angleZ
        }

        private fun decodeAngles(encoded: Int): Triple<Int, Int, Int> {
            val angleX = (encoded shr 16) and 0xFF
            val angleY = (encoded shr 8) and 0xFF
            val angleZ = encoded and 0xFF
            return Triple(angleX, angleY, angleZ)
        }

    }
    private fun contains(point: Point3D): Boolean {
        val localPoint = point - center
        val xExtent = abs(localPoint.dot(u))
        val yExtent = abs(localPoint.dot(v))
        val zExtent = abs(localPoint.dot(w))
        return xExtent <= extents.x && yExtent <= extents.y && zExtent <= extents.z
    }

    fun debugDrawOBB(world: World, obb: OBB, debugOffset: BlockPos) {
        // Calculate the corners of the OBB
        val corners = arrayOf(
            obb.center + (obb.u * obb.extents.x) + (obb.v * obb.extents.y) + (obb.w * obb.extents.z),
            obb.center - (obb.u * obb.extents.x) + (obb.v * obb.extents.y) + (obb.w * obb.extents.z),
            obb.center + (obb.u * obb.extents.x) - (obb.v * obb.extents.y) + (obb.w * obb.extents.z),
            obb.center - (obb.u * obb.extents.x) - (obb.v * obb.extents.y) + (obb.w * obb.extents.z),
            obb.center + (obb.u * obb.extents.x) + (obb.v * obb.extents.y) - (obb.w * obb.extents.z),
            obb.center - (obb.u * obb.extents.x) + (obb.v * obb.extents.y) - (obb.w * obb.extents.z),
            obb.center + (obb.u * obb.extents.x) - (obb.v * obb.extents.y) - (obb.w * obb.extents.z),
            obb.center - (obb.u * obb.extents.x) - (obb.v * obb.extents.y) - (obb.w * obb.extents.z)
        )

        // Draw a block at each corner
        for (corner in corners) {
            val blockPos = BlockPos(corner.x.toInt(), corner.y.toInt(), corner.z.toInt())
            world.setBlockState(debugOffset.add(blockPos), Blocks.GLOWSTONE.defaultState)
        }
    }

    operator fun Vec3d.times(scalar: Double): Point3D {
        return Point3D(this.x * scalar, this.y * scalar, this.z * scalar)
    }

    fun voxelize(): BlockPos3DGrid {
        //TODO: Optimize, add planar intersection test, and figure multithreading out
        val maxSize = MAX_OBB_SIZE + 1
        val xRange = -maxSize..maxSize //step 2
        val yRange = -maxSize..maxSize //step 2
        val zRange = -maxSize..maxSize //step 2

        val uDim = (SIZE_U *2+1).toInt()
        val vDim = (SIZE_V *2+1).toInt()
        val lenDim = (LENGTH *2+1).toInt()

        val blockPosArray = BlockPos3DGrid(MAX_OBB_SIZE, MAX_OBB_SIZE, MAX_OBB_SIZE)

        var xIdx = 0
        for (x in xRange) {
            var foundSomeX = false
            var yIdx = 0
            for ( y in yRange) {
                var foundSomeY = false
                var zIdx = 0
                for (z in zRange){
                    val point = Point3D(x.toDouble(), y.toDouble(), z.toDouble())
                    if (contains(point)) {
                        foundSomeY = true
                        zIdx++
                        if(x == 0 && y == 0 && z == 0){
                            blockPosArray.setBlock(xIdx, yIdx, zIdx, null)
                            continue
                        }
                        blockPosArray.setBlock(xIdx, yIdx, zIdx, BlockPos(x, y, z))
                    }
                }
                if (foundSomeY) {
                    yIdx++
                    foundSomeX = true
                }
            }
            if (foundSomeX) {
                xIdx++
            }
        }
        return blockPosArray
    }

}


