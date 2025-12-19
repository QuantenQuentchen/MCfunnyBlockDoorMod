package funnyblockdoormod.funnyblockdoormod.config

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.Modmenu
import io.wispforest.owo.config.annotation.RangeConstraint
import io.wispforest.owo.config.annotation.SectionHeader

@Modmenu(modId = FunnyBlockDoorMod.MOD_ID)
@Config(name = "WorldTweakConfig", wrapperName = "WorldTweakConfig")
class ConfigModel {
    
    @SectionHeader("Field Damage")
    
    @RangeConstraint(min = 0.000001, max = 10.0)
    @JvmField var volumeScale: Double = 1.0 //The Factor by which Volume scale is used in the Damage.

    @RangeConstraint(min = 0.000001, max = 1.0)
    @JvmField var volumeScaleStep: Double = .1 //The step size for volume scale damage multiplier increase.

    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var volumeScaleClamp: Double = 1000.0 //The maximum value the damage multiplier for volume scale can be.

    @RangeConstraint(min = 0.000001, max = 1.0)
    @JvmField var damageDampeningMax: Double = .5 //The Maximum amount damage is dampened based on projected collapse percentage.
    @JvmField var activateVolumeDamageScale: Boolean = true //Whether the protection Volume positively scales the damage.
    @JvmField var activateDamageDampening: Boolean = true //Whether the damage is dampened based on projected collapse percentage.

    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var attackEscalationRatio: Int = 100
    
    @SectionHeader("Player Interaction Drain")

    //Block Break
    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var blockBreakDamage: Int = 2

    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var blockBreakToolDamage: Int = 10

    //Teleport
    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var teleportDamage: Int = 5

    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var teleportPlayerDamage: Float = 10f

    //Use Prevention
    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var useDamage: Int = 1

    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var usePlayerDamage: Int = 1

    //Block Place
    @RangeConstraint(min = 0.0, max = 999999.9)
    @JvmField var placeDamage: Int = 1
    @JvmField var voidPlacedBlock: Boolean = true //This should maybe default to false.

}