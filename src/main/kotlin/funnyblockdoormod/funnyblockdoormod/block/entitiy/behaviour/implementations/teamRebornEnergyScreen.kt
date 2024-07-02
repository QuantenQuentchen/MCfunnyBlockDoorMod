package funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IenergyScreenBehaviour
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.imgPartInfo
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.ButtonIcon
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

    private val energyTabTexture = Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_v2.png")

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

    private val cornerTopLeft: imgPartInfo = imgPartInfo(0, 32, 4, 4)
    private val cornerTopRight: imgPartInfo = imgPartInfo(88, 32, 4, 4)
    private val cornerBotRight: imgPartInfo = imgPartInfo(88, 130, 4, 4)
    private val cornerBotLeft: imgPartInfo = imgPartInfo(0, 130, 4, 4)

    private val borderTop: imgPartInfo = imgPartInfo(4, 32, 0, 4,84)
    private val borderBot: imgPartInfo = imgPartInfo(4, 131, 0, 4, 84)
    private val borderLeft: imgPartInfo = imgPartInfo(0, 36, 4, 0, 93)
    private val borderRight: imgPartInfo = imgPartInfo(88, 36, 4, 0, 93)

    private fun overlayBorders(ctx: DrawContext, extensionX: Int, extensionY: Int, extension: Int){
        val posX = parent.getTabsRowX()
        val posY = parent.getTabsRowY()

        ctx.drawTexture(energyTabTexture, posX, posY,cornerTopLeft.x, cornerTopLeft.y, cornerTopLeft.sizeX!!, cornerTopLeft.sizeY!!)
        ctx.drawTexture(energyTabTexture, posX, posY + extensionY-5, cornerBotLeft.x, cornerBotLeft.y, cornerBotLeft.sizeX!!, cornerBotLeft.sizeY!!)
        ctx.drawTexture(energyTabTexture, posX + extensionX - cornerBotRight.sizeX!!, posY + extensionY-5, cornerBotRight.x, cornerBotRight.y, cornerBotRight.sizeX, cornerBotRight.sizeY!!)
        ctx.drawTexture(energyTabTexture, posX + extensionX - cornerTopRight.sizeX!!, posY, cornerTopRight.x, cornerTopRight.y, cornerTopRight.sizeX, cornerTopRight.sizeY!!)

        ctx.drawTexture(energyTabTexture, posX+cornerTopLeft.sizeX, posY, borderTop.x, borderTop.y, borderTop.getExtension(extension), borderTop.sizeY!!)
        ctx.drawTexture(energyTabTexture, posX+cornerTopLeft.sizeX, posY+extensionY-1-4, borderBot.x, borderBot.y-1, borderBot.getExtension(extension), borderBot.sizeY!!)

        ctx.drawTexture(energyTabTexture, posX, posY+cornerTopLeft.sizeY, borderLeft.x, borderLeft.y, borderLeft.sizeX!!, borderLeft.getExtension(extension))
        ctx.drawTexture(energyTabTexture, posX+extensionX-4, posY+cornerTopLeft.sizeY, borderRight.x, borderRight.y, borderRight.sizeX!!, borderRight.getExtension(extension))
        
    }

    private val maxExtensionSteps = 20

    private val energyTabWidth = 91
    private val energyTabHeight = 102

    private val energyTabWidthFactor = energyTabWidth.toFloat() / maxExtensionSteps
    private val energyTabHeightFactor = energyTabHeight.toFloat() / maxExtensionSteps

    private val energyTabU = 0
    private val energyTabV = 32

    private var isExtending = false
    private var isRetracting = false
    private var isExtended = false

    private val energyFullU = 92
    private val energyFullV = 0
    private val energyFullWidth = 71
    private val energyFullHeight = 72

    private val energyEmptyU = 12
    private val energyEmptyV = 36

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
        ctx.drawTexture(energyTabTexture, parent.getTabsRowX(), parent.getTabsRowY(), energyTabU, energyTabV,
            min((currentExtension*energyTabWidthFactor).roundToInt(), energyTabWidth),
            min((currentExtension*energyTabHeightFactor).roundToInt(), energyTabHeight))

        if(isExtending || isRetracting) {
            overlayBorders(
                ctx,
                min((currentExtension * energyTabWidthFactor).roundToInt(), energyTabWidth),
                min((currentExtension * energyTabHeightFactor).roundToInt(), energyTabHeight)-1, currentExtension
            )
        }
        if(isExtended) drawEnergyStorage(ctx)

    }

    private fun drawEnergyStorage(ctx: DrawContext){
        val energyStorage = (handler as DoorEmitterScreenHandler).getEnergyStorage()
        val maxEnergyStorage = 1000//handler.getStack(0).orCreateTag.get("MaxEnergy").asInt
        val energyStoragePercentage: Float = energyStorage.toFloat()/maxEnergyStorage.toFloat()
        val energyStorageHeight = min((energyFullHeight*energyStoragePercentage).roundToInt(), energyFullHeight)
        val y = parent.getTabsRowY() + energyEmptyU + (energyFullHeight - energyStorageHeight)
        val x = parent.getTabsRowX() + energyEmptyV
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