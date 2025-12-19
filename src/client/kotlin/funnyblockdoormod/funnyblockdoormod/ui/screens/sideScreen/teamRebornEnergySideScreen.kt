package funnyblockdoormod.funnyblockdoormod.ui.screens.sideScreen

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.container.imgPartInfo
import funnyblockdoormod.funnyblockdoormod.ui.screens.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screenhandler.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.ui.screens.expandableScreen.ExpandableScreen
import funnyblockdoormod.funnyblockdoormod.ui.widgets.ButtonIcon
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import kotlin.math.min
import kotlin.math.roundToInt

class teamRebornEnergySideScreen(parent: DoorEmitterScreen, handler: ScreenHandler, tabIdx: Int) : ExpandableScreen(
    parent,
    handler,
    tabIdx,
    ButtonIcon(
        Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_v2.png"),
        0, 32,
        91, 103,
    ),
    ButtonIcon(
        Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_v2.png"),
        0, 0,
        29, 31,
    ),
    20,
    Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_v2.png"),
    imgPartInfo(0, 32, 4, 4),
    imgPartInfo(88, 32, 4, 4),
    imgPartInfo(88, 130, 4, 4),
    imgPartInfo(0, 130, 4, 4),

    imgPartInfo(4, 32, 0, 4,84),
    imgPartInfo(4, 131, 0, 4, 84),
    imgPartInfo(0, 36, 4, 0, 93),
    imgPartInfo(88, 36, 4, 0, 93)

) {

    companion object: Factory {
        override fun createScreen(parent: DoorEmitterScreen, handler: DoorEmitterScreenHandler, tabIdx: Int): ExpandableScreen {
            return teamRebornEnergySideScreen(parent, handler, tabIdx)
        }
    }

    private val energyFull = imgPartInfo(92,0, 0, 72,72)
    private val energyEmpty = imgPartInfo(12, 36)

    private val energyFullU = 92
    private val energyFullV = 0
    private val energyFullWidth = 71
    private val energyFullHeight = 72

    private val energyEmptyU = 12
    private val energyEmptyV = 36

    private val energyTabTexture = Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/energy_tab_v2.png")

    override fun whenExpanded(ctx: DrawContext) {
        drawEnergyStorage(ctx)
    }

    override fun whenCollapsed(ctx: DrawContext) {
        //TODO("Not yet implemented")
    }

    override fun whenOverlayed() {
        //TODO("Not yet implemented")
    }

    override fun getBlockedSlots(): List<Int> {
        return listOf()
        //TODO("Not yet implemented")
    }

    override fun onPressed() {
        //TODO("Not yet implemented")
    }

    override fun onReleased(isMouseOver: Boolean) {
        //TODO("Not yet implemented")
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        //TODO("Not yet implemented")
    }

    private fun drawEnergyStorage(ctx: DrawContext){
        val energyStorage = (getHandler() as DoorEmitterScreenHandler).getEnergyStorage()
        val maxEnergyStorage = 1000//handler.getStack(0).orCreateTag.get("MaxEnergy").asInt
        val energyStoragePercentage: Float = energyStorage.toFloat()/maxEnergyStorage.toFloat()
        val energyStorageHeight = min((energyFullHeight*energyStoragePercentage).roundToInt(), energyFullHeight)
        FunnyBlockDoorMod.logger.info("EnergyStorageHeight: $energyStorageHeight")
        val localSpace = getLocalSpace()
        val y = localSpace.y + energyEmptyU + (energyFullHeight - energyStorageHeight)
        val x = localSpace.x + energyEmptyV
        val v = energyFullV + energyFullHeight - energyStorageHeight
        ctx.drawTexture(energyTabTexture, x, y, energyFullU, v, getWidth(), energyStorageHeight)
    }

}