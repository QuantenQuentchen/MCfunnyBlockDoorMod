package funnyblockdoormod.funnyblockdoormod.core.dataClasses


import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockPos3DGrid
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.abs


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
        const val LENGTH = 10.0
        private val obbMap = WeakHashMap<Int, OBB>()

        private val emittingGridMap = WeakHashMap<Int, BlockPos3DGrid>()

        private fun getRotatedOBB(angleX: Float, angleY: Float, angleZ: Float): OBB {
            val rotationCompInt = encodeAngles(angleX.toInt(), angleY.toInt(), angleZ.toInt())
            obbMap[rotationCompInt]?.let { return it }
            val center = Point3D(0.0, 0.0, LENGTH/2).rotateX(angleX).rotateY(angleY).rotateZ(angleZ)
            val u = Vec3d(1.0, 0.0, 0.0).rotateX(angleX).rotateY(angleY).rotateZ(angleZ)
            val v = Vec3d(0.0, 1.0, 0.0).rotateX(angleX).rotateY(angleY).rotateZ(angleZ)
            val w = Vec3d(0.0, 0.0, 1.0).rotateX(angleX).rotateY(angleY).rotateZ(angleZ)
            val extents = Vec3d(SIZE_U / 2, SIZE_V / 2, LENGTH / 2)
            val rotatedObb = OBB(center + Point3D(0.0, 0.0, LENGTH/2), u, v, w, extents)
            obbMap[rotationCompInt] = rotatedObb
            return rotatedObb
        }

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
    /*
    private suspend fun rotate(angleX: Float, angleY: Float, angleZ: Float) = coroutineScope {
        // Translate to origin
        val translatedCenter = center - Point3D(extents.x, extents.y, extents.z)

        val jobs = listOf(
            async(Dispatchers.Default) { translatedCenter.rotateX(angleX).rotateY(angleY).rotateZ(angleZ) },
            async(Dispatchers.Default) { u.rotateX(angleX).rotateY(angleY).rotateZ(angleZ) },
            async(Dispatchers.Default) { v.rotateX(angleX).rotateY(angleY).rotateZ(angleZ) },
            async(Dispatchers.Default) { w.rotateX(angleX).rotateY(angleY).rotateZ(angleZ) }
        )

        val results = jobs.awaitAll()

        // Translate back to original position
        center = (results[0] as Point3D) + Point3D(extents.x, extents.y, extents.z)
        u = results[1] as Vec3d
        v = results[2] as Vec3d
        w = results[3] as Vec3d
    }
*/
    private fun contains(point: Point3D): Boolean {
        val localPoint = point - center
        val xExtent = abs(localPoint.dot(u))
        val yExtent = abs(localPoint.dot(v))
        val zExtent = abs(localPoint.dot(w))
        return xExtent <= extents.x && yExtent <= extents.y && zExtent <= extents.z
    }

    fun voxelize(): BlockPos3DGrid {
        //TODO: Optimize, add planar intersection test, and figure multithreading out
        val maxSize = MAX_OBB_SIZE
        val xRange = -maxSize..maxSize //step 2
        val yRange = -maxSize..maxSize //step 2
        val zRange = -maxSize..maxSize //step 2

        val uDim = (SIZE_U *2+1).toInt()
        val vDim = (SIZE_V *2+1).toInt()
        val lenDim = (LENGTH *2+1).toInt()

        val blockPosArray = BlockPos3DGrid(uDim, vDim, lenDim)

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
