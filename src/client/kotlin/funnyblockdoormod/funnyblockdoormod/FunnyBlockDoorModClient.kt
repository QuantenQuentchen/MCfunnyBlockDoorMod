package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.MOD_ID
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.Ownable
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.AABB
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.Sphere
import funnyblockdoormod.funnyblockdoormod.render.Renderer
import funnyblockdoormod.funnyblockdoormod.ui.screens.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screenhandler.ModScreenHandlers
import funnyblockdoormod.funnyblockdoormod.ui.screens.ForceFieldScreen
import funnyblockdoormod.funnyblockdoormod.ui.screens.WirelessRedstoneScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object FunnyBlockDoorModClient : ClientModInitializer {

    //val SHAPE_UPDATE_PACKET = Identifier(MOD_ID, "shape_update_packet") This isn't even needed
    val SHAPE_DELETION_PACKET = Identifier(MOD_ID, "shape_deletion_packet")
    val SHAPE_ADDITION_PACKET = Identifier(MOD_ID, "shape_addition_packet")

    val FULL_RESEND_PACKET = Identifier(MOD_ID, "shape_full_render_packet")

    override fun onInitializeClient() {

        /* Again shouldn't be needed
        ClientPlayNetworking.registerGlobalReceiver(SHAPE_UPDATE_PACKET) {
            client, handler, buf, responseSender ->
            val componentNbt = buf.readNbt() ?: return@registerGlobalReceiver

            client.execute {
                val component = FFComponent.deserializeFromNbt(componentNbt) ?: return@execute
                Renderer.modifyShape(component)
            }
        }
        */

        ClientPlayNetworking.registerGlobalReceiver(SHAPE_DELETION_PACKET) {
            client, handler, buf, responseSender ->
            val shapeUUID = buf.readUuid() ?: return@registerGlobalReceiver
            client.execute {
                Renderer.removeShape(shapeUUID)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(SHAPE_ADDITION_PACKET){
            client, handler, buf, responseSender ->
            val componentNbt = buf.readNbt() ?: return@registerGlobalReceiver
            client.execute {
                val component = FFComponent.deserializeFromNbt(componentNbt) ?: return@execute
                Renderer.addShape(component)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(FULL_RESEND_PACKET){
            client, handler, buf, responseSender ->
            val packetNbt = buf.readNbt()
            val componentList = packetNbt?.getList("componentList", NbtElement.COMPOUND_TYPE.toInt()) ?: return@registerGlobalReceiver
            client.execute {
                Renderer.clear()
                for (component in componentList) {
                    val componentNbt = (component as? NbtCompound) ?: continue
                    val componentObj = FFComponent.deserializeFromNbt(componentNbt) ?: continue
                    Renderer.addShape(componentObj)
                }
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            Renderer.clear()
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            val pos = BlockPos(32, 71, -29)
            val min = pos.add(-80, -80, -80)
            val max = pos.add(80, 80, 80)
            val aabb = AABB(min, max)
            val sphere = Sphere(pos, 80.0)
            val comp1 = FFComponent(aabb, Ownable(), null)
            val comp2 = FFComponent(sphere, Ownable(), null)
            Renderer.addShape(comp1)
            Renderer.addShape(comp2)
        }

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		HandledScreens.register(ModScreenHandlers.DOOREMITTERSCREENHANDLER, ::DoorEmitterScreen)

		HandledScreens.register(ModScreenHandlers.WIRELESS_REDSTONE_SCREEN_HANDLER, ::WirelessRedstoneScreen)

        HandledScreens.register(ModScreenHandlers.FORCE_FIELD_SCREEN_HANDLER, ::ForceFieldScreen)

        Renderer.register()
	}
}