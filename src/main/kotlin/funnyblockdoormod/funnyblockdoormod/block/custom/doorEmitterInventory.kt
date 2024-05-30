package funnyblockdoormod.funnyblockdoormod.block.custom

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.InventoryDepthChange
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import java.lang.ref.WeakReference

class doorEmitterInventory(): Inventory{

    companion object {
        fun fromNbt(nbt: NbtCompound): doorEmitterInventory {
            val inventory = doorEmitterInventory()
            val nbtListZ = nbt.getList("inventory", 10)
            for (z in nbtListZ.indices) {
                val nbtListY = nbtListZ.getList(z)
                for (y in nbtListY.indices) {
                    val nbtListX = nbtListY.getList(y)
                    for (x in nbtListX.indices) {
                        val itemNbt = nbtListX.getCompound(x)
                        inventory.inventory[z][y][x] = ItemStack.fromNbt(itemNbt)
                    }
                }
            }
            inventory.x = nbt.getInt("x")
            inventory.y = nbt.getInt("y")
            inventory.z = nbt.getInt("z")
            inventory.state = nbt.getInt("state")
            inventory.layer = nbt.getInt("layer")
            return inventory
        }
    }

    val inventory: Array<Array<Array<ItemStack?>>> = Array(25) { Array(5) { arrayOfNulls(5) } }

    private val middle = ((inventory[0].size + 1) / 2) -1

    private var x = middle
    private var y = middle
    private var z = 0
    private var state = 0
    private var layer = 0
    private val listeners = mutableListOf<WeakReference<InventoryChangedListener>>()
    private val depthListeners = mutableListOf<WeakReference<InventoryDepthChange>>()

    var depth = 0
        set(value) {
            field = value
            if (value < 0) {
                field = 0
            }
            if (value >= inventory.size) {
                field = inventory.size - 1
            }
            depthListeners.forEach { it.get()?.onDepthChange(this.depth) }
        }

    fun addListener(listener: InventoryChangedListener) {
        listeners.add(WeakReference(listener))
    }

    fun removeListener(listener: InventoryChangedListener) {
        listeners.removeAll { it.get() == listener }
    }

    fun iterator(): Iterator<ItemStack?> {
        return object : Iterator<ItemStack?> {

            override fun hasNext(): Boolean {
                return z < inventory.size
            }

            override fun next(): ItemStack? {
                val item = when (state) {
                    0 -> inventory.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    1 -> inventory.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
                    2 -> inventory.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    3 -> inventory.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    else -> null
                }
                state = (state + 1) % 4
                if (state == 0) {
                    layer++
                    if (layer > x || layer > y) {
                        layer = 0
                        if (z < inventory.size - 1) {
                            z++
                        }
                    }
                }
                return item
            }
        }
    }

    fun reverseIterator(): Iterator<ItemStack?> {
        return object : Iterator<ItemStack?> {

            override fun hasNext(): Boolean {
                return z >= 0
            }

            override fun next(): ItemStack? {
                val item = when (state) {
                    0 -> inventory.getOrNull(z)?.getOrNull(y + layer)?.getOrNull(x) // Down
                    1 -> inventory.getOrNull(z)?.getOrNull(y)?.getOrNull(x - layer) // Left
                    2 -> inventory.getOrNull(z)?.getOrNull(y - layer)?.getOrNull(x) // Up
                    3 -> inventory.getOrNull(z)?.getOrNull(y)?.getOrNull(x + layer) // Right
                    else -> null
                }
                state = (state + 1) % 4
                if (state == 0) {
                    layer++
                    if (layer > x || layer > y) {
                        layer = 0
                        if (z > 0) {
                            z--
                        }
                    }
                }
                return item
            }
        }
    }

    fun toNbt(): NbtCompound {
        val nbtListZ = NbtList()
        for (z in inventory.indices) {
            val nbtListY = NbtList()
            for (y in inventory[z].indices) {
                val nbtListX = NbtList()
                for (x in inventory[z][y].indices) {
                    val itemStack = inventory[z][y][x]
                    val itemNbt = NbtCompound()
                    itemStack?.writeNbt(itemNbt)
                    nbtListX.add(itemNbt)
                }
                nbtListY.add(nbtListX)
            }
            nbtListZ.add(nbtListY)
        }

        val nbtCompound = NbtCompound()
        nbtCompound.put("inventory", nbtListZ)
        nbtCompound.putInt("x", x)
        nbtCompound.putInt("y", y)
        nbtCompound.putInt("z", z)
        nbtCompound.putInt("state", state)
        nbtCompound.putInt("layer", layer)

        return nbtCompound
    }

    override fun clear() {
        for (z in inventory.indices) {
            for (y in inventory[z].indices) {
                for (x in inventory[z][y].indices) {
                    inventory[z][y][x] = null
                }
            }
        }
        markDirty()
    }

    override fun size(): Int {
        return inventory.size * inventory[0].size * inventory[0][0].size
    }

    override fun isEmpty(): Boolean {
        for (z in inventory.indices) {
            for (y in inventory[z].indices) {
                for (x in inventory[z][y].indices) {
                    if (inventory[z][y][x] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun getStack(slot: Int): ItemStack {
        val cords = convertTo3D(slot)
        val x = cords.first
        val y = cords.second
        val z = cords.third
        return inventory.getOrNull(z)?.getOrNull(y)?.getOrNull(x) ?: ItemStack.EMPTY
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        FunnyBlockDoorMod.logger.warn("Tries to remove $amount items from slot $slot")

        val cords = convertTo3D(slot)
        val x = cords.first
        val y = cords.second
        val z = cords.third

        FunnyBlockDoorMod.logger.warn("Removing items from slot $x, $y, $z")

        val itemStack = getStack(slot)
        if (itemStack == ItemStack.EMPTY) return ItemStack.EMPTY

        val result = itemStack.copy()

        if (itemStack.count <= amount) {
            setStack(slot, null)
        } else {
            result.count = amount
            itemStack.decrement(amount)
        }
        markDirty()
        return result

    }

    override fun removeStack(slot: Int): ItemStack {
        val itemStack = getStack(slot)
        if (itemStack != ItemStack.EMPTY) {
            val result = itemStack.copy()
            setStack(slot, null)
            markDirty()
            return result
        }
        return ItemStack.EMPTY
    }

    private fun convertTo1D(x: Int, y: Int, z: Int): Int {
        return x + y * inventory[0][0].size + z * inventory[0][0].size * inventory[0].size
    }

    private fun convertTo3D(index: Int): Triple<Int, Int, Int> {
        val x = index % inventory[0][0].size
        val y = (index / inventory[0][0].size) % inventory[0].size
        //val z = index / (inventory[0][0].size * inventory[0].size)
        return Triple(x, y, depth)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        val cords = convertTo3D(slot)
        val x = cords.first
        val y = cords.second
        val z = cords.third
        inventory[z][y][x] = stack
        markDirty()
    }

    override fun markDirty() {
        listeners.forEach { it.get()?.onInventoryChanged(this) }
        listeners.removeAll { it.get() == null }
    }

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return true
    }

}
