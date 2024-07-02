package funnyblockdoormod.funnyblockdoormod.core.containerClasses

import kotlin.math.min

data class imgPartInfo(val x: Int, val y: Int, val sizeX: Int? = null, val sizeY: Int? = null, val maxExtension: Int? = null){

    private val extensionFactor = maxExtension?.toFloat()?.div(20) ?: 0.0f

    fun getExtension(extension: Int): Int {
        return min((extension * extensionFactor).toInt(), maxExtension ?: extension)
    }

}
