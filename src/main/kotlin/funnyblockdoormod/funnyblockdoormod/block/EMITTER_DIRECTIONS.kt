package funnyblockdoormod.funnyblockdoormod.block

import net.minecraft.util.math.Vec3d

enum class EMITTER_DIRECTIONS(val vector: Vec3d) {
    NOT_SET(Vec3d(0.0, 0.0, 0.0)),
    UP(Vec3d(0.0, 1.0, 0.0)),
    DOWN(Vec3d(0.0, -1.0, 0.0)),
    NORTH(Vec3d(0.0, 0.0, -1.0)),
    EAST(Vec3d(1.0, 0.0, 0.0)),
    SOUTH(Vec3d(0.0, 0.0, 1.0)),
    WEST(Vec3d(-1.0, 0.0, 0.0));
}