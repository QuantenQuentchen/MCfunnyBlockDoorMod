package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviourFactory
import funnyblockdoormod.funnyblockdoormod.block.entitiy.doorEmitterBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleEnergyStorage


class teamRebornEnergy(private val blockEntity: BlockEntity): IenergyBehaviour {


    companion object : IenergyBehaviourFactory {
        override fun create(blockEntity: BlockEntity): IenergyBehaviour {
            return teamRebornEnergy(blockEntity)
        }

        override fun registerEnergyStorage(blockEntityType: BlockEntityType<*>) {
            EnergyStorage.SIDED.registerForBlockEntity(
                { blockEntity, _ -> (blockEntity as? doorEmitterBlockEntity)?.energyBehaviour?.energyStorage as? SimpleEnergyStorage},
                blockEntityType
            )
        }

    }

    override val energyStorage: SimpleEnergyStorage by lazy {
        object : SimpleEnergyStorage(1000, 1000, 1000) {
            override fun onFinalCommit() {
                blockEntity.markDirty()
            }
        }
    }

    override fun init() {

    }

    override val energyType = IenergyBehaviour.EnergyType.TEAM_REBORN

    override fun afterTypeCreation() {
        EnergyStorage.SIDED.registerForBlockEntity(
            { _, _ -> this.energyStorage},
            blockEntity.type
        )
    }


    override fun update() {
        blockEntity.markDirty()
    }

    override fun canConsume(amount: Int): Boolean {
        return energyStorage.amount >= amount
    }

    override fun consume(amount: Int): Boolean {
        energyStorage.amount -= amount
        return true
    }

    override fun getEnergy(): Long? {
        return energyStorage.amount
    }
}