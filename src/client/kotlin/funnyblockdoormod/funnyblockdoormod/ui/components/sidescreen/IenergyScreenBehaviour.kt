package funnyblockdoormod.funnyblockdoormod.ui.components.sidescreen

import funnyblockdoormod.funnyblockdoormod.ui.screens.DoorEmitterScreen
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