package funnyblockdoormod.funnyblockdoormod.screen

import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.teamRebornEnergyScreen
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.ButtonIcon
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IconButtonWidget
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.IconToggleWidget
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.LimitedNumberFieldWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class DoorEmitterScreen(handler: DoorEmitterScreenHandler, inventory:PlayerInventory, title: Text)
    : HandledScreen<DoorEmitterScreenHandler>(handler, inventory, title) {

        private val texture = Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/door_emitter_gui_legacy.png")

    private val maxAngle = 360
    private val minAngle = 0

    private val upBtnX = 0
    private val upBtnY = 0

    private val downBtnX = 50
    private val downBtnY = 50

    private val xAngleInputX = 10
    private val xAngleInputY = 27
    private lateinit var xAngleInput: LimitedNumberFieldWidget

    private val yAngleInputX = 10
    private val yAngleInputY = 61
    private lateinit var yAngleInput: LimitedNumberFieldWidget

    private val zAngleInputX = 10
    private val zAngleInputY = 95
    private lateinit var zAngleInput: LimitedNumberFieldWidget


    private val incrButtonWidth = 13
    private val incrButtonHeight = 9
    private val incrButtonHeightOffset = 14

    private val invDepthIncrX = 136
    private val invDepthIncrY = 45

    private val invDepthX = 136
    private val invDepthY = 34

    private val xAngleIncrX = 16
    private val xAngleIncrY = 16

    private val yAngleIncrX = 16
    private val yAngleIncrY = 50

    private val zAngleIncrX = 16
    private val zAngleIncrY = 84

    private val inputWidth = 25
    private val inputHeight = 10

    private val TabsRowX = 176
    private val TabsRowY = 3

    private val TabsContentX = TabsRowX + 29
    private val TabsContentY = TabsRowY - 3

    fun getTabsRowX(): Int {
        return x + TabsRowX
    }

    fun getTabsRowY(): Int {
        return y + TabsRowY
    }

    fun getTabsContentX(): Int {
        return x + TabsContentX
    }

    fun getTabsContentY(): Int {
        return y + TabsContentY
    }

    private val upIcon = ButtonIcon.getButtonIcon(
        texture,
        176, 17,
        9, 5,
    )

    private val downIcon = ButtonIcon.getButtonIcon(
        texture,
        176, 22,
        9, 5,
    )

    private val blockedSlot = ButtonIcon.getButtonIcon(
        texture,
        176, 0,
        16, 16,
    )

    private val energysubScreen = teamRebornEnergyScreen(this, handler)

    override fun init() {
        super.init()
        addInputFields()
        this.playerInventoryTitleX = 32113
        this.playerInventoryTitleY = 0
        // Add invDepth increment buttons
        addIncrementButtons(invDepthIncrX, invDepthIncrY, {handler.incrementInvDepth()}, {handler.decrementInvDepth()})

        // Add Angle increment buttons
        addIncrementButtons(xAngleIncrX, xAngleIncrY, { handler.decrementXAngle() }, { handler.incrementXAngle() })
        addIncrementButtons(yAngleIncrX, yAngleIncrY, { handler.decrementYAngle() }, { handler.incrementYAngle() })
        addIncrementButtons(zAngleIncrX, zAngleIncrY, { handler.decrementZAngle() }, { handler.incrementZAngle() })

        energysubScreen.registerEnergyScreenTab(getTabsRowX(), getTabsRowY())
    }


    fun addButton(widget: IconButtonWidget): IconButtonWidget {
        addDrawableChild(widget)
        addSelectableChild(widget)
        return widget
    }

    fun addButton(widget: IconToggleWidget): IconToggleWidget {
        addDrawableChild(widget)
        addSelectableChild(widget)
        return widget
    }

    private fun addInputFields() {
        val xCords = transformToGui(xAngleInputX, xAngleInputY)
        xAngleInput = LimitedNumberFieldWidget(textRenderer, xCords.first, xCords.second, inputWidth, inputHeight)
        xAngleInput.setMaxLength(3)
        xAngleInput.minNumber = minAngle
        xAngleInput.maxNumber = maxAngle

        val yCord = transformToGui(yAngleInputX, yAngleInputY)
        yAngleInput = LimitedNumberFieldWidget(textRenderer, yCord.first, yCord.second, inputWidth, inputHeight)
        yAngleInput.setMaxLength(3)
        yAngleInput.minNumber = minAngle
        yAngleInput.maxNumber = maxAngle

        val zCord = transformToGui(zAngleInputX, zAngleInputY)
        zAngleInput = LimitedNumberFieldWidget(textRenderer, zCord.first, zCord.second, inputWidth, inputHeight)
        zAngleInput.setMaxLength(3)
        zAngleInput.minNumber = minAngle
        zAngleInput.maxNumber = maxAngle


        addDrawableChild(xAngleInput)
        addSelectableChild(xAngleInput)

        addDrawableChild(yAngleInput)
        addSelectableChild(yAngleInput)

        addDrawableChild(zAngleInput)
        addSelectableChild(zAngleInput)
    }

    private fun addIncrementButtons(
        startX: Int,
        startY: Int,
        upAction: ButtonWidget.PressAction,
        downAction: ButtonWidget.PressAction,
        offsetOffset: Int = 0
    ){

        val downY = startY + incrButtonHeight + incrButtonHeightOffset + offsetOffset

        val transformedUpCoords = transformToGui(startX, startY)
        val upBtnX = transformedUpCoords.first
        val upBtnY = transformedUpCoords.second

        val transformedDownCoords = transformToGui(startX, downY)
        val downBtnX = transformedDownCoords.first
        val downBtnY = transformedDownCoords.second

        val upBtn = IconButtonWidget.builder(upIcon, upAction)
            .position(upBtnX, upBtnY)
            .size(incrButtonWidth, incrButtonHeight)
            .build()

        val downBtn = IconButtonWidget.builder(downIcon, downAction)
            .position(downBtnX, downBtnY)
            .size(incrButtonWidth, incrButtonHeight)
            .build()

        addDrawableChild(upBtn)
        addSelectableChild(upBtn)

        addDrawableChild(downBtn)
        addSelectableChild(downBtn)
    }

    init {
        backgroundHeight = 201
        //backgroundWidth = 190
    }

    fun getX(): Int {
        return x
    }

    fun getY(): Int {
        return y
    }

    private fun transformToGui(x: Int, y: Int): Pair<Int, Int> {
        val guiX = (width - backgroundWidth) / 2
        val guiY = (height - backgroundHeight) / 2
        val transformedX = guiX + x
        val transformedY = guiY + y
        return Pair(transformedX, transformedY)
    }

    private fun transformToGui(cords: Pair<Int, Int>): Pair<Int, Int> {
        val guiX = (width - backgroundWidth) / 2
        val guiY = (height - backgroundHeight) / 2
        val transformedX = guiX + cords.first
        val transformedY = guiY + cords.second
        return Pair(transformedX, transformedY)
    }

    private fun getGridSlotPosition(slotId: Int): Pair<Int, Int> {
        val index = slotId - 0
        val j = index % 5 // column index
        val i = index / 5 // row index
        val x = 8 + j * 17
        val y = 44 + i * 17
        return Pair(x, y)
    }

    private fun drawBlockSlots(ctx: DrawContext){
        val blockedSlots = handler.getBlockedSlots()
        for(slot in blockedSlots){
            val cords = transformToGui(getGridSlotPosition(slot))
            val x = cords.first
            val y = cords.second
            ctx.drawTexture(blockedSlot.identifier, x, y, blockedSlot.u, blockedSlot.v, blockedSlot.textureWidth, blockedSlot.textureHeight)
        }
    }

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionProgram)
        RenderSystem.setShaderColor(1f,1f,1f,1f)
        RenderSystem.setShaderTexture(0, texture)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        context?.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    private fun renderText(context: DrawContext?, text: String, x: Int, y: Int) {
        val textCords = transformToGui(x, y)
        context?.drawText(textRenderer, text, textCords.first, textCords.second, 0xFFFFFF, false)
    }

    private fun renderNum(context: DrawContext?, number: Int, x: Int, y: Int) {
        val textCords = transformToGui(x, y)
        context?.drawText(textRenderer, number.toString(), textCords.first, textCords.second, 0xFFFFFF, false)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)

        if (context != null) {
            energysubScreen.drawEnergyScreen(context)
        }
        super.render(context, mouseX, mouseY, delta)
        renderNum(context, handler.getInvDepth(), invDepthX, invDepthY)
        // Calculate the position of the text relative to the GUI
        drawBlockSlots(context!!)
        // Draw the text

        drawMouseoverTooltip(context, mouseX, mouseY)
    }
}