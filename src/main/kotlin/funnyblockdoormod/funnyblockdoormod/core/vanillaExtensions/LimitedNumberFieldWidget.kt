package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.widget.TextFieldWidget

class LimitedNumberFieldWidget(textRenderer: TextRenderer,
                               x: Int,
                               y: Int,
                               width: Int,
                               height: Int,
                               private val correctCharLambda: ((Int) -> Unit)? = null
) : TextFieldWidget(textRenderer, x, y, width, height, null) {

    var maxNumber: Int = 360
    var minNumber: Int = 0
    private var number: Int = 0
        set(value) {
            text = value.toString()
            field = value
            }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (chr.isDigit() || (chr == '-' && this.text.isEmpty())) {
            val potentialText = this.text + chr
            val num = potentialText.toIntOrNull()

            if (num != null && num in minNumber..maxNumber) {
                number = num
                correctCharLambda?.let { it(num) }
                return super.charTyped(chr, modifiers)
            }
        }
        return false
    }

    override fun write(text: String) {
        //Kill the default write method
    }

}