package funnyblockdoormod.funnyblockdoormod.screen

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class DoorEmitterScreen(handler: DoorEmitterScreenHandler, inventory:PlayerInventory, title: Text)
    : HandledScreen<DoorEmitterScreenHandler>(handler, inventory, title) {

        private val texture = Identifier("funnyblockdoormod", "textures/gui/door_emitter_gui_legacy.png")

    private val invDepthX = 137
    private val invDepthY = 56

    private val upBtnX = 0
    private val upBtnY = 0

    private val downBtnX = 50
    private val downBtnY = 50


    override fun init() {
        super.init()
        val upAction: ButtonWidget.PressAction = ButtonWidget.PressAction { button ->
            handler.incrementInvDepth()
        }
        val downAction: ButtonWidget.PressAction = ButtonWidget.PressAction { button ->
            handler.decrementInvDepth()
        }

        val transformedUpCoords = transformToGui(upBtnX, upBtnY)
        val upBtnX = transformedUpCoords.first
        val upBtnY = transformedUpCoords.second

        val transformedDownCoords = transformToGui(downBtnX, downBtnY)
        val downBtnX = transformedDownCoords.first
        val downBtnY = transformedDownCoords.second

        val upBtn = ButtonWidget.builder(Text.of(""), upAction).position(upBtnX, upBtnY).build()
        val downBtn = ButtonWidget.builder(Text.of(""), downAction).position(downBtnX, downBtnY).build()
        addDrawableChild(upBtn)
        addDrawableChild(downBtn)

    }



    init {
        backgroundHeight = 201
    }

    private fun transformToGui(x: Int, y: Int): Pair<Int, Int> {
        val guiX = (width - backgroundWidth) / 2
        val guiY = (height - backgroundHeight) / 2
        val transformedX = guiX + x
        val transformedY = guiY + y
        return Pair(transformedX, transformedY)
    }


    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionProgram)
        RenderSystem.setShaderColor(1f,1f,1f,1f)
        RenderSystem.setShaderTexture(0, texture)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        context?.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val invDepth = handler.getInvDepth()

        // Calculate the position of the text relative to the GUI


        // Draw the text
        val textCords = transformToGui(invDepthX, invDepthY)
        val textX = textCords.first
        val textY = textCords.second
        context?.drawText(textRenderer, invDepth.toString(), textX, textY, 0xFFFFFF, false)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }
}