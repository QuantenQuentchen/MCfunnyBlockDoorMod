package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.IntPoint2D
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text

open class IconButtonWidget(x:Int, y:Int, width: Int, height: Int, private val onPressAction: PressAction,
                            protected val defaultIcon: ButtonIcon, private val hoverIcon: ButtonIcon,
                            protected var pressIcon: ButtonIcon, private val disabledIcon: ButtonIcon,
) : PressableWidget(x, y, width, height, Text.of("")) {

    private var textureX: Int = 0
    private var textureY: Int = 0

    private var centerX: Int = 0
    private var centerY: Int = 0

    protected var icon: ButtonIcon = defaultIcon

    private val clickMap: Set<IntPoint2D> = icon.clickMap

    companion object {
        private var mcInstance: MinecraftClient? = null

        fun builder(icon: ButtonIcon, onPressAction: PressAction): Builder {
            return Builder(icon, onPressAction)
        }

        class Builder(private val defaultIcon: ButtonIcon, private val onPressAction: PressAction) {
            private var x: Int = 0
            private var y: Int = 0
            private var width: Int = 0
            private var height: Int = 0
            private var hoverIcon: ButtonIcon = defaultIcon
            private var pressIcon: ButtonIcon = defaultIcon
            private var disabledIcon: ButtonIcon = defaultIcon
            private var activeIcon: ButtonIcon = defaultIcon

            fun position(x: Int, y: Int): Builder {
                this.x = x
                this.y = y
                return this
            }

            fun hoverIcon(icon: ButtonIcon): Builder {
                this.hoverIcon = icon
                return this
            }

            fun pressIcon(icon: ButtonIcon): Builder {
                this.pressIcon = icon
                return this
            }

            fun disabledIcon(icon: ButtonIcon): Builder {
                this.disabledIcon = icon
                return this
            }

            fun activeIcon(icon: ButtonIcon): Builder {
                this.activeIcon = icon
                return this
            }

            fun size(width: Int, height: Int): Builder {
                this.width = width
                this.height = height
                return this
            }

            fun build(): IconButtonWidget {
                return IconButtonWidget(x, y, width, height, onPressAction, defaultIcon, hoverIcon, pressIcon, disabledIcon)
            }
        }
    }

    init {
        if (mcInstance == null) { mcInstance = MinecraftClient.getInstance() }
        updateTexturePosition()
    }

    private fun updateTexturePosition() {
        // Calculate the center of the button
        centerX = x + width / 2
        centerY = y + height / 2

        // Calculate the top-left corner of the texture
        textureX = centerX - icon.textureWidth / 2
        textureY = centerY - icon.textureHeight / 2
    }

    fun resize(newWidth: Int, newHeight: Int){
        width = newWidth
        height = newHeight
        updateTexturePosition()
    }

    fun setIconPress(icon: ButtonIcon){
        pressIcon = icon
    }

    private fun setIconDefault(){
        icon = defaultIcon
    }
    private fun setIconHover(){
        icon = hoverIcon
    }
    private fun setIconPress(){
        icon = pressIcon
    }
    private fun setIconDisabled(){
        icon = disabledIcon
    }

    fun disable(){
        setIconDisabled()
        this.active = false
    }

    fun enable(){
        setIconDefault()
        this.active = true
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        this.appendDefaultNarrations(builder)
    }

    override fun onPress() {
        setIconPress()
        onPressAction.onPress(null)

    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        super.onRelease(mouseX, mouseY)
        if(isMouseOver(mouseX, mouseY)){
            setIconDefault()
        }
    }

    override fun renderButton(ctx: DrawContext,x: Int, y: Int, tickDelta: Float) {

        ctx.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()

        ctx.drawTexture(icon.identifier, textureX, textureY, icon.u, icon.v, icon.textureWidth, icon.textureHeight)

    }

    private fun transformToLocalSpace(mouseX: Double, mouseY: Double): IntPoint2D {

        val localX = (mouseX - centerX + icon.textureWidth / 2).toInt()
        val localY = (mouseY - centerY + icon.textureHeight / 2).toInt()

        return IntPoint2D(localX, localY)
    }

    override fun clicked(mouseX: Double, mouseY: Double): Boolean {
        val localPoint = transformToLocalSpace(mouseX, mouseY)
        return this.active && this.visible && clickMap.contains(localPoint)
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val localPoint = transformToLocalSpace(mouseX, mouseY)
        return this.active && this.visible && clickMap.contains(localPoint)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        super.mouseMoved(mouseX, mouseY)
        if (isMouseOver(mouseX, mouseY)) {
            setIconHover()
        //mcInstance?.currentScreen?.renderTooltip(icon.identifier, mouseX.toInt(), mouseY.toInt())
        }
    }

}