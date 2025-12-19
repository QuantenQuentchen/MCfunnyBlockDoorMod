package funnyblockdoormod.funnyblockdoormod.mixin;

import funnyblockdoormod.funnyblockdoormod.events.custom.TeleportEvent;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Main mixin for player teleportation - handles /tp command and direct teleport() calls
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Unique
    private ServerWorld teleportOriginWorld;

    /**
     * This is the main teleport method that /tp command and most mods use
     * Signature: teleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch)
     */
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void beforeTeleport(
            ServerWorld targetWorld,
            double destX, double destY, double destZ,
            java.util.Set<PositionFlag> flags,
            float yaw, float pitch,
            CallbackInfoReturnable<Boolean> cir
    ) {
        //TODO: Test if this breaks something
        //if (cir.isCancelled()) return;

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld origin = player.getServerWorld();

        // Store origin for AFTER event
        teleportOriginWorld = origin;

        // Create TeleportTarget for the event
        Vec3d pos = new Vec3d(destX, destY, destZ);
        Vec3d velocity = player.getVelocity();
        TeleportTarget target = new TeleportTarget(pos, velocity, yaw, pitch);

        ActionResult result = TeleportEvent.BEFORE_TELEPORT.invoker()
                .beforeTeleport(player, targetWorld, target);

        if (result == ActionResult.FAIL) {
            teleportOriginWorld = null; // Clean up
            cir.setReturnValue(false); // Return false to indicate teleport failed
        }
    }

    @Inject(
            method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At("RETURN")
    )
    private void afterTeleport(
            ServerWorld targetWorld,
            double destX, double destY, double destZ,
            java.util.Set<PositionFlag> flags,
            float yaw, float pitch,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Only fire AFTER event if teleport was successful
        if (cir.getReturnValue() && teleportOriginWorld != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

            TeleportEvent.AFTER_TELEPORT.invoker()
                    .afterTeleport(player, teleportOriginWorld, targetWorld);

            teleportOriginWorld = null;
        }
    }

    /**
     * Handles ender pearls, chorus fruit, and other teleportation requests
     * Signature: requestTeleport(double x, double y, double z)
     */
    @Inject(
            method = "requestTeleport",
            at = @At("HEAD"),
            cancellable = true
    )
    private void beforeRequestTeleport(double x, double y, double z, CallbackInfo ci) {
        //TODO: Test if this breaks something
        //if (ci.isCancelled()) return;
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ServerWorld world = player.getServerWorld();

        Vec3d pos = new Vec3d(x, y, z);
        Vec3d velocity = player.getVelocity();
        TeleportTarget target = new TeleportTarget(
                pos, velocity,
                player.getYaw(), player.getPitch()
        );

        ActionResult result = TeleportEvent.BEFORE_TELEPORT.invoker()
                .beforeTeleport(player, world, target);

        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}