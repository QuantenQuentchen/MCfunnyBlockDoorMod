package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import net.minecraft.world.World

object stupidDimensionRegistry {
    private val registry: MutableMap<RegistryKey<World>, Int> = mutableMapOf()
    fun register(key: RegistryKey<World>): Int {
        return registry.getOrPut(key) {
            registry.size
        }
    }

    fun unregister(key: RegistryKey<World>) {
        registry.remove(key)
    }

    fun getId(key: RegistryKey<World>): Int? {
        return registry[key]
    }

    fun getKey(id: Int): RegistryKey<World>? {
        return registry.entries.find { it.value == id }?.key
    }
}