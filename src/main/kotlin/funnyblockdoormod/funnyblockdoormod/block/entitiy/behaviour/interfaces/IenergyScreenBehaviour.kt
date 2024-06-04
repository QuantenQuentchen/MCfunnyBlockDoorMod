package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces

import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.ScreenHandler

interface IenergyScreenBehaviour {

    val parent: DoorEmitterScreen

    val handler: ScreenHandler

    fun drawEnergyScreen(ctx: DrawContext)

    fun registerEnergyScreenButtons()

    fun registerEnergyScreenTab(x: Int, y: Int)

    fun registerSlots()

    fun extendTab()

    fun retractTab()

    fun getBlockedSlots()



}