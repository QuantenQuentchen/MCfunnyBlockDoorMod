package funnyblockdoormod.funnyblockdoormod.events

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.CONFIG
import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.logger
import funnyblockdoormod.funnyblockdoormod.data.FFPermission
import funnyblockdoormod.funnyblockdoormod.data.bvh.BVH
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.Sphere
import funnyblockdoormod.funnyblockdoormod.events.custom.ExplosionEvent
import funnyblockdoormod.funnyblockdoormod.events.custom.TeleportEvent
import funnyblockdoormod.funnyblockdoormod.serialize.Serializable
import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.entity.EquipmentSlot
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.round

object EventHandler {
    
    fun registerEventListeners() {

        ServerChunkEvents.CHUNK_UNLOAD.register { _, _ -> Serializable.saveState() }
        ServerWorldEvents.UNLOAD.register { _, _ -> Serializable.saveState() }
        ServerLifecycleEvents.SERVER_STOPPING.register { Serializable.saveState() }
        ServerLifecycleEvents.SERVER_STOPPED.register { Serializable.clearCaches() }



        PlayerBlockBreakEvents.BEFORE.register { world, player, pos: BlockPos, state, blockEntity ->
            if (world.isClient || player.isSpectator) return@register true
            //Need to figure out what exactly the blockEntity is here, I would assume the potentially broken block, but I am not sure
            val fieldPrim = getForceFieldAt(pos, world as ServerWorld) ?: return@register true
            val field = fieldPrim.ownership?.owner ?: return@register true

            if (!field.getPermission(player.uuid).has(FFPermission.BREAK)) {
                player.sendMessage(Text.literal("Block Break suppressed"))
                if (field.attackField(CONFIG.blockBreakDamage(), fieldPrim, pos, world)) return@register true
                player.mainHandStack.damage(CONFIG.blockBreakToolDamage(), player) { e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
                return@register false
            }
            return@register true
        }

        ExplosionEvent.BEFORE_EXPLOSION.register { world, entity, x, y, z, power, createFire, destructionType ->
            if (world.isClient || entity == null) return@register ActionResult.PASS
            val explosionVolume = Sphere(
                x,
                y,
                z,
                power * 2.0
            )

            val affectedRegions = explosionVolume.getCoveredRegions()
            if (affectedRegions.isEmpty()) return@register ActionResult.PASS

            var allowed = true
            for (regionPos in affectedRegions) {
                val bvh = BVH.getBVH(world as ServerWorld, regionPos) ?: continue
                val fieldsAndVol = bvh.getOverlapScalarTotal(explosionVolume)
                val maxVolume = fieldsAndVol.second
                for ((field, overlap) in fieldsAndVol.first){
                    field.ownership?.owner?.attackField(round(overlap.overlapScalar/maxVolume).toInt(), field, overlap.shrinkVector, world) ?: continue
                    allowed = false
                }
            }

            if (allowed) {
                return@register ActionResult.PASS
            }

            return@register ActionResult.FAIL
        }

        TeleportEvent.BEFORE_TELEPORT.register { entity, destination, target ->
            val fieldPrim = getForceFieldAt(target.position, destination) ?: return@register ActionResult.PASS
            val field = fieldPrim.ownership?.owner ?: return@register ActionResult.PASS

            if (!field.getPermission(entity.uuid).has(FFPermission.TELEPORT)) {
                entity.sendMessage(Text.literal("Teleport suppressed"))
                if (field.attackField(CONFIG.teleportDamage(), fieldPrim, target.position, destination)) return@register ActionResult.PASS
                entity.damage(entity.world.damageSources.generic(), CONFIG.teleportPlayerDamage())
                return@register ActionResult.FAIL
            }
            return@register ActionResult.PASS
        }

        logger.info("EventHandler initialized")
    }

    private fun getForceFieldAt(blockPos: BlockPos, world: ServerWorld): FFComponent? {
        val bvh = BVH.getBVH(world, RegionPos.fromBlock(blockPos))
        return bvh?.getVolume(blockPos)
    }
    private fun getForceFieldAt(x: Int, y: Int, z: Int, world: ServerWorld): FFComponent? {
        val bvh = BVH.getBVH(world, RegionPos.fromBlock(x, z))
        return bvh?.getVolume(BlockPos(x, y, z))
    }

    private fun getForceFieldAt(x: Double, y: Double, z: Double, world: ServerWorld): FFComponent? =
        getForceFieldAt(x.toInt(), y.toInt(), z.toInt(), world)

    private fun getForceFieldAt(vec: Vec3d, world: ServerWorld): FFComponent? =
        getForceFieldAt(vec.x.toInt(), vec.y.toInt(), vec.z.toInt(), world)
}