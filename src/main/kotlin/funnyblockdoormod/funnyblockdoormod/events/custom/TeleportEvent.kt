package funnyblockdoormod.funnyblockdoormod.events.custom

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.TeleportTarget

object TeleportEvent {

    /**
     * Called before an entity teleports. Allows cancellation and modification.
     *
     * If any listener returns FAIL, the teleport is cancelled.
     * If any listener returns SUCCESS, the teleport proceeds (overrides FAIL).
     * If all listeners return PASS, the teleport proceeds normally.
     */
    @JvmField
    val BEFORE_TELEPORT: Event<BeforeTeleport> = EventFactory.createArrayBacked(
        BeforeTeleport::class.java
    ) { callbacks ->
        BeforeTeleport { entity, destination, target ->
            for (callback in callbacks) {
                val result = callback.beforeTeleport(entity, destination, target)

                when (result) {
                    ActionResult.FAIL -> return@BeforeTeleport ActionResult.FAIL // Cancel teleport
                    ActionResult.SUCCESS -> return@BeforeTeleport ActionResult.SUCCESS // Force allow
                    else -> continue
                }
            }
            ActionResult.PASS // Default behavior
        }
    }

    /**
     * Called after an entity has successfully teleported.
     * Cannot cancel the teleport as it has already occurred.
     */
    @JvmField
    val AFTER_TELEPORT: Event<AfterTeleport> = EventFactory.createArrayBacked(
        AfterTeleport::class.java
    ) { callbacks ->
        AfterTeleport { entity, origin, destination ->
            for (callback in callbacks) {
                callback.afterTeleport(entity, origin, destination)
            }
        }
    }

    /**
     * Callback interface for before teleport event.
     */
    fun interface BeforeTeleport {
        /**
         * @param entity The entity being teleported
         * @param destination The destination world (may be different from current world)
         * @param target The teleport target containing position, velocity, and rotation
         * @return FAIL to cancel, SUCCESS to force allow, PASS to continue
         */
        fun beforeTeleport(
            entity: Entity,
            destination: ServerWorld,
            target: TeleportTarget
        ): ActionResult
    }

    /**
     * Callback interface for after teleport event.
     */
    fun interface AfterTeleport {
        /**
         * @param entity The entity that was teleported
         * @param origin The origin world
         * @param destination The destination world
         */
        fun afterTeleport(
            entity: Entity,
            origin: ServerWorld,
            destination: ServerWorld
        )
    }

    /**
     * Helper data class to store teleport origin information
     */
    data class TeleportOrigin(
        val world: ServerWorld,
        val position: Vec3d,
        val yaw: Float,
        val pitch: Float
    )
}