package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType

interface IenergyBehaviourFactory {
    fun create(blockEntity: BlockEntity): IenergyBehaviour

    fun registerEnergyStorage(blockEntityType: BlockEntityType<*>)
}