package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import net.minecraft.util.math.Vec3d
import java.util.UUID

operator fun Vec3d.minus(other: Vec3d): Vec3d = this.subtract(other)
operator fun Vec3d.plus(other: Vec3d): Vec3d = this.add(other)
operator fun Vec3d.plus(other: Double): Vec3d = this.add(other, other, other)
operator fun Vec3d.times(other: Double): Vec3d = this.multiply(other)

fun UUID.invalid(): UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")