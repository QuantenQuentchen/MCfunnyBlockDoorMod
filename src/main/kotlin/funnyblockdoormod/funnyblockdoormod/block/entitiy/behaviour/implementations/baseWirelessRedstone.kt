package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneBehaviour
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneReciever
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IwirelessRedstoneRecieverNum
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockPosDim
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.stupidDimensionRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.lang.ref.WeakReference

object baseWirelessRedstone: IwirelessRedstoneBehaviour {


    val MaxChannels = 256

    private val entityChannels: MutableMap<Int, MutableList<WeakReference<IwirelessRedstoneReciever>>> = mutableMapOf()
    private val blockInstanceChannels: MutableList<WeakReference<IwirelessRedstoneRecieverNum>> = mutableListOf()

    private val genericMap: MutableMap<Int, Boolean> = mutableMapOf()

    lateinit var wirelessRedstoneState: WirelessRedstoneState

    override fun init() {

    }

    fun getChannel(channel: Int): Boolean {
        return genericMap[channel] ?: false
    }

    override fun setChannel(channel: Int, isActive: Boolean) {
        genericMap[channel] = isActive

        entityChannels[channel]?.forEach { it.get()?.onChannelChange(isActive) }
        wirelessRedstoneState.blockChannels[channel]?.forEach { posDim -> blockInstanceChannels.forEach{
            it.get()?.let {blockInstance->
                val world = MinecraftClient.getInstance().server?.getWorld(wirelessRedstoneState.getKey(posDim.dim))?: return
                 blockInstance.onChannelChange(isActive, posDim.pos, world)
                }
            }
        }
    }

    override fun subscribeToChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever) {
        entityChannels.getOrPut(channel) { mutableListOf() }.add(WeakReference(redstoneReciever))
    }

    override fun subscribe(redstoneReciever: IwirelessRedstoneRecieverNum) {

        blockInstanceChannels.add(WeakReference(redstoneReciever))
    }

    override fun subscribePosToChannel(channel: Int, pos: BlockPos, dim: RegistryKey<World>) {
        val dimId = wirelessRedstoneState.register(dim)
        val posDim = BlockPosDim(pos, dimId)
        wirelessRedstoneState.blockChannels.getOrPut(channel) { mutableListOf() }.add(posDim)
        wirelessRedstoneState.markDirty()
    }

    override fun unsubscribeFromChannel(channel: Int, redstoneReciever: IwirelessRedstoneReciever) {
        entityChannels[channel]?.removeIf { it.get() == redstoneReciever }
    }

    override fun unsubscribe(redstoneReciever: IwirelessRedstoneRecieverNum) {
        blockInstanceChannels.removeIf { it.get() == redstoneReciever }
    }

    override fun unsubscribePosFromChannel(channel: Int, pos: BlockPos, dim: RegistryKey<World>) {
        val dimId = wirelessRedstoneState.register(dim)
        val posDim = BlockPosDim(pos, dimId)
        wirelessRedstoneState.blockChannels[channel]?.remove(posDim)
        wirelessRedstoneState.markDirty()
    }
}