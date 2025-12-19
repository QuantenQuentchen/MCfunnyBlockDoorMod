package funnyblockdoormod.funnyblockdoormod.serialize

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import org.slf4j.LoggerFactory

class TestSerializable(
    private val region: String,
    private val key: String,
    var data: String
) : Serializable() {

    override fun writeNBT(): NbtElement {
        val tag = NbtCompound()
        tag.putString("data", data)
        return tag
    }

    override fun getKey(): String = key

    override fun getRegion(): String = region

    companion object {
        fun fromNBT(tag: NbtElement, region: String, key: String): TestSerializable {
            val compound = tag as NbtCompound
            return TestSerializable(
                "test_region",
                "test_key",
                compound.getString("data")
            )
        }
    }
}