package funnyblockdoormod.funnyblockdoormod.data.ffShape.shape

import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d

sealed interface IShape {
    enum class ShapeType(val id: String){
        AABB("AABB"),
        SPHERE("SPHERE"),
        SPHEROID("SPHEROID"),
        ERROR("ERROR");

        companion object {
            private val BY_ID = entries.associateBy { it.id }
            fun fromId(id: String?): ShapeType = BY_ID[id] ?: ERROR
        }

    }


    interface Factory {
        fun deserializeFromNbt(tag: NbtCompound): IShape?

        companion object {
            fun serializeToNbt(area: IShape): NbtCompound {
                val tag = NbtCompound()
                tag.putString("type", area.type.id)
                val nested = NbtCompound()
                area.serializeToNbt(nested)
                tag.put("area", nested)
                return tag
            }

            fun deserializeFromNbt(tag: NbtCompound): IShape? {
                val type = ShapeType.fromId(tag.getString("type"))
                return when (type) {
                    ShapeType.ERROR -> null
                    ShapeType.AABB -> AABB.deserializeFromNbt(tag.getCompound("area"))
                    ShapeType.SPHERE -> Sphere.deserializeFromNbt(tag.getCompound("area"))
                    ShapeType.SPHEROID -> null
                }
            }
        }
    }

    val volume: Double
    val type: ShapeType
    val centerI: BlockPos
    val centerD: Vec3d

    fun getBoundingBox(): AABB
    fun getCoveredChunks(): List<ChunkPos>
    fun getCoveredRegions(): List<RegionPos>

    fun serializeToNbt(tag: NbtCompound = NbtCompound()): NbtCompound

    fun containsPoint(pos: BlockPos): Boolean

    /*
    Intersection Double Dispatch
    */
    fun intersects(other: IShape): Boolean
    //Visitors
    fun intersectsWith(aabb: AABB): Boolean
    fun intersectsWith(sphere: Sphere): Boolean
    //fun intersectsSpheroid(other: FFSpheroid): Boolean

    /*
    Intersection Scalar Double Dispatch
    */
    fun intersectionVolume(other: IShape): Double
    //Visitors
    fun intersectionVolumeWith(aabb: AABB): Double
    fun intersectionVolumeWith(sphere: Sphere): Double
    //fun intersectionVolumeSpheroid(other: FFSpheroid): Double

    /*
    Shrink Vector Double Dispatch
     */
    fun computeOwnShrinkVector(other: IShape): Vec3d
    //Fucking cursed flipping method to keep operand order.
    fun computeOwnShrinkVectorFlipped(other: IShape): Vec3d

    //Visitors
    fun computeOwnShrinkVectorWith(aabb: AABB): Vec3d
    fun computeOwnShrinkVectorWith(sphere: Sphere): Vec3d

    fun shrinkByVector(vector: Vec3d): IShape?

    fun getShrinkVectorFromBlockHit(pos: BlockPos): Vec3d

    fun shrinkVectorPercentage(vector: Vec3d): Double?

    fun toUIString(): String

}