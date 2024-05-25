package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneReciever
import java.lang.ref.WeakReference

object baseWirelessRedstone: IwirelessRedstoneBehaviour {

    private val channels: MutableMap<Int, MutableList<WeakReference<IwirelessRedstoneReciever>>> = mutableMapOf()

    override fun init() {
    }

    override fun setChannel(channel: Int, isActive: Boolean) {
        channels[channel]?.forEach { it.get()?.onChannelChange(isActive) }
    }

    override fun subscribeToChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever) {
        channels.getOrPut(channel) { mutableListOf() }.add(WeakReference(redstoneReciever))
    }

    override fun unsubscribeFromChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever) {
        channels[channel]?.removeIf { it.get() == redstoneReciever }
    }
}