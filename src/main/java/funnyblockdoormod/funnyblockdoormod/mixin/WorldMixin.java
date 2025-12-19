package funnyblockdoormod.funnyblockdoormod.mixin;

import funnyblockdoormod.funnyblockdoormod.events.custom.ExplosionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onExplosionBefore(
            @Nullable Entity entity,
            @Nullable net.minecraft.entity.damage.DamageSource damageSource,
            @Nullable net.minecraft.world.explosion.ExplosionBehavior behavior,
            double x, double y, double z,
            float power,
            boolean createFire,
            World.ExplosionSourceType explosionSourceType,
            boolean particles,
            CallbackInfoReturnable<Explosion> cir
    ) {
        World world = (World) (Object) this;

        ActionResult result = ExplosionEvent.BEFORE_EXPLOSION.invoker()
                .beforeExplosion(world, entity, x, y, z, power, createFire, explosionSourceType);

        if (result == ActionResult.FAIL) {
            // Cancel the explosion by creating an empty explosion with no affected blocks
            Explosion emptyExplosion = new Explosion(
                    world, entity, damageSource, behavior,
                    x, y, z, 0.0F, // Set power to 0
                    false, Explosion.DestructionType.KEEP
            );
            emptyExplosion.getAffectedBlocks().clear(); // Ensure no blocks are affected
            cir.setReturnValue(emptyExplosion);
        }
    }

    @Inject(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At("RETURN")
    )
    private void onExplosionAfter(
            @Nullable Entity entity,
            @Nullable net.minecraft.entity.damage.DamageSource damageSource,
            @Nullable net.minecraft.world.explosion.ExplosionBehavior behavior,
            double x, double y, double z,
            float power,
            boolean createFire,
            World.ExplosionSourceType explosionSourceType,
            boolean particles,
            CallbackInfoReturnable<Explosion> cir
    ) {
        Explosion explosion = cir.getReturnValue();
        if (explosion != null) {
            World world = (World) (Object) this;
            ExplosionEvent.ON_EXPLOSION.invoker()
                    .onExplosion(world, entity, x, y, z, power, explosion);
        }
    }
}