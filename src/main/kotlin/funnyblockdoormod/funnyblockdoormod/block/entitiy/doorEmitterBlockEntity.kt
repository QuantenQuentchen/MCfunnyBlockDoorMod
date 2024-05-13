package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.block.EMITTER_DIRECTIONS
import funnyblockdoormod.funnyblockdoormod.block.doorBlockData
import funnyblockdoormod.funnyblockdoormod.block.doorBlockDataGrid
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.BlockState3DGrid
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.OBB
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class doorEmitterBlockEntity : BlockEntity, ExtendedScreenHandlerFactory, ImplementedInventory {

    companion object {
        private val INPUT_SLOT = 0
        private val OUTPUT_SLOT = 1
    }

    constructor(pos: BlockPos, state: BlockState) : super(ModBlockEntities.DOOR_EMITTER_BLOCK_ENTITY, pos, state){
        this.propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> progress
                    1 -> maxProgress
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> progress = value
                    1 -> maxProgress = value
                }
            }

            override fun size(): Int {
                return 2
            }
        }
    }

    private val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(2, ItemStack.EMPTY)

    protected val propertyDelegate: PropertyDelegate
    private var progress: Int = 0
    private var maxProgress: Int = 73

    private var lastTickTime = System.currentTimeMillis()

    private val currentXAngle: Float = 0f
    private val currentYAngle: Float = 0f
    private val currentZAngle: Float = 0f

    private var currentEmittingGrid = OBB.getEmittingGrid(currentXAngle, currentYAngle, currentZAngle)
    private var currentGridBlocks = BlockState3DGrid(25, 25, 25)


    override fun getDisplayName(): Text {
        return Text.literal("Door Emitter") //change to translatable later
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf?) {
        if (buf != null) {
            buf.writeBlockPos(this.pos)
        }
    }

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        Inventories.writeNbt(nbt, inventory)
        nbt?.putInt("progress", progress)
    }

    override fun markDirty() {
        super<BlockEntity>.markDirty()
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        Inventories.readNbt(nbt, inventory)
        progress = nbt?.getInt("progress") ?: 0
    }

    override fun getItems(): DefaultedList<ItemStack> {
        TODO("Not yet implemented")
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        TODO("Not yet implemented")
    }

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if(world.isClient) return

        val currentTickTime = System.currentTimeMillis()
        val operationMultiplier = (currentTickTime - lastTickTime) / 20

        //Tick Logic







        //End Tick Logic
        lastTickTime = currentTickTime
    }

}