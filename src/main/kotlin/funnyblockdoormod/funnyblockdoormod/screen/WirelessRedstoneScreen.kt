package funnyblockdoormod.funnyblockdoormod.screen

import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.LimitedNumberFieldWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class WirelessRedstoneScreen(handler: WirelessRedstoneScreenHandler, inventory: PlayerInventory, title: Text)
    : HandledScreen<WirelessRedstoneScreenHandler>(handler, inventory, title) {


    companion object {
        val texture = Identifier(FunnyBlockDoorMod.MOD_ID, "textures/gui/wireless_redstone_gui_bg.png")
    }

    init {
        backgroundWidth = 165
        backgroundHeight = 60
    }

    override fun init() {
        super.init()

        this.playerInventoryTitleX = 32113
        this.playerInventoryTitleY = 0

        val channel = LimitedNumberFieldWidget(
            textRenderer,
            transformToGui(20, 15).first,
            transformToGui(20, 15).second,
            80,
            15
        )
        channel.number = handler.getChannel() ?: 0

        val applyButton = ButtonWidget.Builder(Text.of("Apply")) { handler.setChannel(channel.number) }
            .position(transformToGui(20, 32).first, transformToGui(20, 32).second)
            .size(80, 20)
            .build()

        addDrawableChild(channel)
        addDrawableChild(applyButton)

        addSelectableChild(channel)
        addSelectableChild(applyButton)
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

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionProgram)
        RenderSystem.setShaderColor(1f,1f,1f,1f)
        RenderSystem.setShaderTexture(0, texture)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        context?.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }
}