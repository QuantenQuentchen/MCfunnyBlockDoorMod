package funnyblockdoormod.funnyblockdoormod.core.dataClasses

import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Double, val y: Double, val z: Double) {

    constructor(vec: Vec3d): this(vec.x, vec.y, vec.z)

    fun rotateX(angle: Float): Point3D {
        val rad = Math.toRadians(angle.toDouble())
        val cos = cos(rad)
        val sin = sin(rad)
        return Point3D(
            x = x,
            y = y * cos - z * sin,
            z = y * sin + z * cos
        )
    }

    operator fun plus (other: Point3D): Point3D {
        return Point3D(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Point3D): Point3D {
        return Point3D(x - other.x, y - other.y, z - other.z)
    }

    operator fun minus(other: Vec3d): Point3D {
        return Point3D(x - other.x, y - other.y, z - other.z)
    }

    fun dotProduct(other: Point3D): Double {
        return x * other.x + y * other.y + z * other.z
    }

    fun dotProduct(other: Vec3d): Double {
        return x * other.x + y * other.y + z * other.z
    }

    fun dot(vec: Vec3d): Double {
        return x * vec.x + y * vec.y + z * vec.z
    }

    fun rotateY(angle: Float): Point3D {
        val rad = Math.toRadians(angle.toDouble())
        val cos = cos(rad)
        val sin = sin(rad)
        return Point3D(
            x = z * sin + x * cos,
            y = y,
            z = z * cos - x * sin
        )
    }

    fun rotateZ(angle: Float): Point3D {
        val rad = Math.toRadians(angle.toDouble())
        val cos = cos(rad)
        val sin = sin(rad)
        return Point3D(
            x = x * cos - y * sin,
            y = x * sin + y * cos,
            z = z
        )
    }

    fun toVec3d(): Vec3d {
        return Vec3d(x, y, z)
    }

}