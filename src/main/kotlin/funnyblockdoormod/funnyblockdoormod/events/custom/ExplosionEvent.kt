package funnyblockdoormod.funnyblockdoormod.events.custom

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity
import net.minecraft.util.ActionResult
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion

object ExplosionEvent {

    /**
     * Called before an explosion occurs. Allows cancellation.
     *
     * If any listener returns FAIL, the explosion is cancelled.
     * If any listener returns SUCCESS, the explosion proceeds (overrides FAIL).
     * If all listeners return PASS, the explosion proceeds normally.
     */
    @JvmField
    val BEFORE_EXPLOSION: Event<BeforeExplosion> = EventFactory.createArrayBacked(
        BeforeExplosion::class.java
    ) { callbacks ->
        BeforeExplosion { world, entity, x, y, z, power, createFire, destructionType ->
            for (callback in callbacks) {
                val result = callback.beforeExplosion(world, entity, x, y, z, power, createFire, destructionType)

                when (result) {
                    ActionResult.FAIL -> return@BeforeExplosion ActionResult.FAIL // Cancel explosion
                    ActionResult.SUCCESS -> return@BeforeExplosion ActionResult.SUCCESS // Force allow
                    else -> continue
                }
            }
            ActionResult.PASS // Default behavior
        }
    }

    /**
     * Called after an explosion has occurred but before blocks are destroyed.
     * Cannot cancel the explosion, but can modify affected blocks.
     */
    @JvmField
    val ON_EXPLOSION: Event<OnExplosion> = EventFactory.createArrayBacked(
        OnExplosion::class.java
    ) { callbacks ->
        OnExplosion { world, entity, x, y, z, power, explosion ->
            for (callback in callbacks) {
                callback.onExplosion(world, entity, x, y, z, power, explosion)
            }
        }
    }

    /**
     * Callback interface for before explosion event.
     */
    fun interface BeforeExplosion {
        /**
         * @param world The world where the explosion occurs
         * @param entity The entity causing the explosion (can be null)
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @param power Explosion power
         * @param createFire Whether the explosion creates fire
         * @param destructionType The type of destruction (KEEP, DESTROY, DESTROY_WITH_DECAY)
         * @return FAIL to cancel, SUCCESS to force allow, PASS to continue
         */
        fun beforeExplosion(
            world: World,
            entity: Entity?,
            x: Double,
            y: Double,
            z: Double,
            power: Float,
            createFire: Boolean,
            destructionType: World.ExplosionSourceType
        ): ActionResult
    }

    /**
     * Callback interface for after explosion calculation.
     */
    fun interface OnExplosion {
        /**
         * @param world The world where the explosion occurs
         * @param entity The entity causing the explosion (can be null)
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @param power Explosion power
         * @param explosion The explosion object (can modify affectedBlocks)
         */
        fun onExplosion(
            world: World,
            entity: Entity?,
            x: Double,
            y: Double,
            z: Double,
            power: Float,
            explosion: Explosion
        )
    }
}