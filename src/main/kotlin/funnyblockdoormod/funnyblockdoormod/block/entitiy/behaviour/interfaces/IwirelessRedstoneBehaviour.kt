package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

interface IwirelessRedstoneBehaviour {
    fun init()

    fun setChannel(channel: Int, isActive: Boolean)

    fun subscribeToChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever)

    fun unsubscribeFromChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever)

}