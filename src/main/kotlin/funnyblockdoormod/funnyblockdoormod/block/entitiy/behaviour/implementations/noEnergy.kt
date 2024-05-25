package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyBehaviourFactory
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType

class noEnergy(blockEntity: BlockEntity): IenergyBehaviour {

    companion object : IenergyBehaviourFactory {
        override fun create(blockEntity: BlockEntity): IenergyBehaviour {
            return noEnergy(blockEntity)
        }
        override fun registerEnergyStorage(blockEntityType: BlockEntityType<*>) {
            return
        }
    }

    override val energyType = IenergyBehaviour.EnergyType.NONE

    override val energyStorage: Any?
        get() = null

    override fun init() {
        return
    }

    override fun update() {
        return
    }

    override fun canConsume(amount: Int): Boolean {
        return true
    }

    override fun consume(amount: Int): Boolean {
        return true
    }

    override fun afterTypeCreation() {
        return
    }

    override fun getEnergy(): Long? {
        return null
    }
}