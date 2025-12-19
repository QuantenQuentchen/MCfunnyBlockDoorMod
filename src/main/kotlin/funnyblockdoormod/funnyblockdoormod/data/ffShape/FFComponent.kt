package funnyblockdoormod.funnyblockdoormod.data.ffShape

import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape.Factory
import funnyblockdoormod.funnyblockdoormod.serialize.Serializable
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.Vec3d
import java.util.UUID

class FFComponent private constructor(
    val shape: IShape,
    val ownership: Ownable? = null,
    val collapsible: Collapsible? = null,
    val visualization: Visualization? = null,
    val activatable: Activatable? = null,
    fromNbt: Boolean = false
): Serializable() {

    init {
        if (!fromNbt && ownership != null){
            markDirty()
        }
    }

    constructor(
        shape: IShape,
        ownership: Ownable? = null, collapsible: Collapsible? = null,
        visualization: Visualization? = null, activatable: Activatable? = null
    ): this(shape, ownership, collapsible, visualization, activatable, false)



    fun shrinkByVector(vector: Vec3d): FFComponent? {
        val shrunkShape = this.shape.shrinkByVector(vector) ?: return null
        return FFComponent(shrunkShape, this.ownership, this.collapsible)
    }

    fun serializeToNbt(tag: NbtCompound): NbtCompound {
        tag.put("Shape", Factory.serializeToNbt(shape))
        if (ownership != null) tag.put("Ownership", ownership.serializeToNbt(NbtCompound()))
        if (collapsible != null) tag.put("Collapsible", collapsible.serializeToNbt(NbtCompound()))
        if (visualization != null) tag.put("Visualization", visualization.serializeToNbt(NbtCompound()))
        if (activatable != null) tag.put("Activatable", activatable.serializeToNbt(NbtCompound()))
        return tag
    }

    override fun writeNBT(): NbtElement {
        return serializeToNbt(NbtCompound())
    }

    override fun getKey(): String {
        return ownership?.uuid.toString()
    }

    override fun getRegion(): String {
        return REGION_NAME
    }

    companion object {

        suspend fun getFromUUID(uuid: String): FFComponent?{
            return Serializable.get(Serializable.Companion.RootRef.WEAK, REGION_NAME, uuid) {
                tag: NbtElement, region, key -> deserializeFromNbt(tag as NbtCompound) as Serializable
            } as FFComponent?
        }

        private const val REGION_NAME = "FFComponent"
        fun deserializeFromNbt(tag: NbtCompound): FFComponent? {

            val shape: IShape = tag.get("Shape")?.let {
                if (it is NbtCompound) Factory.deserializeFromNbt(it) else return null
            } ?: return null

            val ownership: Ownable? = tag.get("Ownership")?.let {
                if (it is NbtCompound) Ownable.deserializeFromNbt(it) else null
            }

            val collapsible: Collapsible? = tag.get("Collapsible")?.let {
                if (it is NbtCompound) Collapsible.deserializeFromNbt(it) else null
            }

            val visualization: Visualization? = tag.get("Visualization")?.let {
                if (it is NbtCompound) Visualization.deserializeFromNbt(it) else null
            }

            val activatable: Activatable? = tag.get("Activatable")?.let {
                if (it is NbtCompound) Activatable.deserializeFromNbt(it) else null
            }

            return FFComponent(shape, ownership, collapsible, visualization, activatable, fromNbt = true)
        }
    }
}