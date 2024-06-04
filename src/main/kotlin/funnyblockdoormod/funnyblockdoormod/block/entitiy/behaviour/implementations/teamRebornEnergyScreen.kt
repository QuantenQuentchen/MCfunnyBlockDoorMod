package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyScreenBehaviour
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.IntPoint2D
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.ButtonIcon
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IconButtonWidget
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IconToggleWidget
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import kotlin.math.min
import kotlin.math.roundToInt

class teamRebornEnergyScreen(override val parent: DoorEmitterScreen, override val handler: ScreenHandler) : IenergyScreenBehaviour {

    private val energyTabTexture = Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_legacy.png")

    private val deactiveEnergyTab = ButtonIcon.getButtonIcon(
        energyTabTexture,
        0, 0,
        29, 31,
    )
    private val pressedEnergyTab = ButtonIcon.getButtonIcon(
        energyTabTexture,
        0, 32,
        32, 31,
    )

    private val maxExtensionSteps = 20

    private val energyTabWidth = 109
    private val energyTabHeight = 201

    private val energyTabWidthFactor = energyTabWidth.toFloat() / maxExtensionSteps
    private val energyTabHeightFactor = energyTabHeight.toFloat() / maxExtensionSteps

    private val energyTabU = 34
    private val energyTabV = 0

    private var isExtending = false
    private var isRetracting = false
    private var isExtended = false

    private val energyFullU = 143
    private val energyFullV = 0
    private val energyFullWidth = 71
    private val energyFullHeight = 151

    private var currentExtension = 0

    private lateinit var energyTab: PressableWidget

    override fun extendTab(){
        isExtending = true
        //energyTab.setIconPress(pressedEnergyTab)
    }

    private fun toggleTab(){
        if(isExtended) retractTab()
        else extendTab()
    }

    override fun retractTab(){
        isExtended = false
        isRetracting = true
    }

    override fun getBlockedSlots() {

    }

    override fun drawEnergyScreen(ctx: DrawContext) {
        if(isExtending) extend()
        if(isRetracting) retract()
        //if(!isExtending || !isRetracting)
        ctx.drawTexture(energyTabTexture, parent.getTabsContentX(), parent.getTabsContentY(), energyTabU, energyTabV,
            min((currentExtension*energyTabWidthFactor).roundToInt(), energyTabWidth),
            min((currentExtension*energyTabHeightFactor).roundToInt(), energyTabHeight))

        if(isExtended) drawEnergyStorage(ctx)

    }

    private fun drawEnergyStorage(ctx: DrawContext){
        val energyStorage = (handler as DoorEmitterScreenHandler).getEnergyStorage()
        val maxEnergyStorage = 1000//handler.getStack(0).orCreateTag.get("MaxEnergy").asInt
        val energyStoragePercentage: Float = energyStorage.toFloat()/maxEnergyStorage.toFloat()
        val energyStorageHeight = min((energyFullHeight*energyStoragePercentage).roundToInt(), energyFullHeight)
        val y = parent.getTabsContentY() + 12 + (energyFullHeight - energyStorageHeight)
        val x = parent.getTabsContentX() + 19
        val v = energyFullV + energyFullHeight - energyStorageHeight
        ctx.drawTexture(energyTabTexture, x, y, energyFullU, v, energyTabWidth, energyStorageHeight)
    }

    private fun extend(){
        currentExtension++
        if(currentExtension > maxExtensionSteps) {
            isExtending = false
            isExtended = true
        }
    }

    private fun retract(){
        currentExtension--
        if(currentExtension < 0) isRetracting = false
    }

    override fun registerEnergyScreenButtons() {

    }

    override fun registerEnergyScreenTab(x: Int, y: Int) {
        val btn = IconToggleWidget.builder(deactiveEnergyTab) { this.toggleTab() }
            .position(x, y)
            .size(29, 31)
            .pressIcon(pressedEnergyTab)
            .build()


        energyTab = parent.addButton(btn)
    }

    override fun registerSlots() {
        //TODO("Not yet implemented")
    }

}