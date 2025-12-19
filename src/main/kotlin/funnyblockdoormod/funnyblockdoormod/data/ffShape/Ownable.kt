package funnyblockdoormod.funnyblockdoormod.data.ffShape

import funnyblockdoormod.funnyblockdoormod.data.ForceField
import funnyblockdoormod.funnyblockdoormod.data.ServerHolder
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.UUID

class Ownable(
    private val _ownerUUID: UUID,
    private val _uuid: UUID = UUID.randomUUID(),
    private val _worldRegistry: RegistryKey<World>
) {



    val owner: ForceField?
        get() = ForceField.get(_ownerUUID)
    val world: ServerWorld?
        get() = ServerHolder.server?.getWorld(_worldRegistry)
    val uuid: UUID
        get() = this._uuid

    fun serializeToNbt(tag: NbtCompound): NbtCompound{
        tag.putUuid("owner", _ownerUUID)
        tag.putUuid("uuid", _uuid)
        tag.putString("worldIdentifier", _worldRegistry.value.toString())
        return tag
    }

    constructor(): this(UUID.randomUUID(), UUID.randomUUID(), RegistryKey.of(RegistryKeys.WORLD, Identifier("overworld")))

    companion object{
        fun deserializeFromNbt(nbt: NbtCompound): Ownable?{
            val ownerUUID = nbt.getUuid("owner")
            val uuid = nbt.getUuid("uuid")
            val worldIdentifier = nbt.getString("worldIdentifier")
            val worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(worldIdentifier)) ?: return null
            return Ownable(ownerUUID, uuid, worldKey)
        }
    }
    constructor(owner: ForceField, world: ServerWorld): this(_ownerUUID = owner.uuid, _worldRegistry = world.registryKey)
}