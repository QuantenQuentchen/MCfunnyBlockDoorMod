package funnyblockdoormod.funnyblockdoormod.block

import net.minecraft.block.BlockState

data class doorBlockData(
    var state: BlockState? = null,
    var amount: Int? = null,
    var isIndestructible: Boolean? = null
){
    fun placeBlock(){
        //place block
    }
    fun removeBlock(){
        //remove block
    }
}
