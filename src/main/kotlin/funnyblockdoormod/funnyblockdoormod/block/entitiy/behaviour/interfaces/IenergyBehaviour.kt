package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

interface IenergyBehaviour {

    enum class EnergyType {
        NONE,
        TEAM_REBORN
    }

    val energyType: EnergyType

    val energyStorage: Any?

    fun init()

    fun update()

    fun canConsume(amount: Int): Boolean

    fun consume(amount: Int): Boolean

    fun afterTypeCreation()

    fun getEnergy(): Long

}