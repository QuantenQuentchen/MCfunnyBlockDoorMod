package funnyblockdoormod.funnyblockdoormod.ui.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ButtonWidget.PressAction

class IconToggleWidget(x:Int, y:Int, width: Int, height: Int, onPressAction: PressAction, defaultIcon: ButtonIcon,
                       hoverIcon: ButtonIcon, pressIcon: ButtonIcon, disabledIcon: ButtonIcon
) : IconButtonWidget(x,y, width, height, onPressAction, defaultIcon, hoverIcon, pressIcon, disabledIcon) {

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

            fun size(width: Int, height: Int): Builder {
                this.width = width
                this.height = height
                return this
            }

            fun build(): IconToggleWidget {
                return IconToggleWidget(x, y, width, height, onPressAction, defaultIcon, hoverIcon, pressIcon, disabledIcon)
            }
        }
    }

    private var toggled = false

    private fun toggleIcon(){
        icon = if(toggled){
            defaultIcon
        } else {
            pressIcon
        }
        toggled = !toggled
    }

    fun setDisplayedIcon(icon: ButtonIcon){
        defaultIcon = icon
        this.icon = icon
    }

    override fun onPress() {
        super.onPress()
        toggleIcon()
    }

}