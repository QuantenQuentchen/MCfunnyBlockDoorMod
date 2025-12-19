package funnyblockdoormod.funnyblockdoormod.block.entitiy

import funnyblockdoormod.funnyblockdoormod.screenhandler.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.screenhandler.ForceFieldScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.UUID

class ForceFieldBlockEntity: BlockEntity, ExtendedScreenHandlerFactory {

    // Persistent unique ID for serialization and linking
    var uniqueID: UUID = UUID.randomUUID()

    // Example internal state
    var energy: Int = 0
    var maxEnergy = 100

    fun getPercentage(): Double {
        // Example calculation for percentage
        return if (energy >= maxEnergy) 100.0 else (energy.toDouble() / maxEnergy) * 100.0
    }

    constructor(pos: BlockPos, state: BlockState) : super(ModBlockEntities.FORCE_FIELD_BLOCK_ENTITY, pos, state) {
/*        energyBehaviour.afterTypeCreation() // The fuck ?!?

        this.propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> invDepth
                    1 -> encodedSlotStates
                    2 -> energyBehaviour.getEnergy().toInt()
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> invDepth = value
                    1 -> encodedSlotStates = value
                }
            }

            override fun size(): Int {
                return 3
            }
        }*/
    }

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (world.isClient) return

        // Tick logic goes here
        // Example: update ForceField, interact with BVH, etc.
    }

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        tag.putUuid("UniqueID", uniqueID)
        tag.putInt("Energy", energy)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        uniqueID = tag.getUuid("UniqueID")
        energy = tag.getInt("Energy")
    }

    @Environment(EnvType.CLIENT)
    fun clientTick() {
        // Optional client-side tick logic
    }

    override fun writeScreenOpeningData(
        player: ServerPlayerEntity?,
        buf: PacketByteBuf?
    ) {
        buf?.writeBlockPos(this.pos)
    }

    override fun getDisplayName(): Text? {
        return Text.literal("Force Field")
    }

    override fun createMenu(
        syncId: Int,
        playerInventory: PlayerInventory?,
        player: PlayerEntity?
    ): ScreenHandler? {
        return playerInventory?.let { ForceFieldScreenHandler(syncId, it, this) }
    }
}
