package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class IconButtonWidget(x:Int, y:Int, width: Int, height: Int, private val onPressAction: PressAction, var icon: ButtonIcon)
    : PressableWidget(x, y, width, height, Text.of("")) {

    private var textureX: Int = 0
    private var textureY: Int = 0


    companion object {
        private var mcInstance: MinecraftClient? = null

        fun builder(icon: ButtonIcon, onPressAction: PressAction): Builder {
            return Builder(icon, onPressAction)
        }

        class Builder(private val icon: ButtonIcon, private val onPressAction: PressAction) {
            private var x: Int = 0
            private var y: Int = 0
            private var width: Int = 0
            private var height: Int = 0

            fun position(x: Int, y: Int): Builder {
                this.x = x
                this.y = y
                return this
            }

            fun size(width: Int, height: Int): Builder {
                this.width = width
                this.height = height
                return this
            }

            fun build(): IconButtonWidget {
                return IconButtonWidget(x, y, width, height, onPressAction, icon)
            }
        }
    }

    init {
        if (mcInstance == null) { mcInstance = MinecraftClient.getInstance() }
        updateTexturePosition()
    }

    private fun updateTexturePosition() {
        // Calculate the center of the button
        val centerX = x + width / 2
        val centerY = y + height / 2

        // Calculate the top-left corner of the texture
        textureX = centerX - icon.displayWidth / 2
        textureY = centerY - icon.displayHeight / 2
    }

    fun resize(newWidth: Int, newHeight: Int){
        width = newWidth
        height = newHeight
        updateTexturePosition()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        this.appendDefaultNarrations(builder)
    }

    override fun onPress() {
        onPressAction.onPress(null)
    }
    override fun renderButton(ctx: DrawContext,x: Int, y: Int, tickDelta: Float) {
        super.renderButton(ctx, x, y, tickDelta)
        //mcInstance?.textureManager?.bindTexture(icon)
        // Calculate the center of the button

        ctx.drawTexture(icon.identifier, textureX, textureY, icon.u, icon.v, icon.displayWidth, icon.displayHeight)
    }
}