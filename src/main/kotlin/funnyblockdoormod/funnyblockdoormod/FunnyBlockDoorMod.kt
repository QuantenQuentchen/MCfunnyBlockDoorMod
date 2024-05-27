package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import funnyblockdoormod.funnyblockdoormod.block.entitiy.ModBlockEntities
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.teamRebornEnergy
import funnyblockdoormod.funnyblockdoormod.block.entitiy.doorEmitterBlockEntity
import funnyblockdoormod.funnyblockdoormod.item.ModItemGroups
import funnyblockdoormod.funnyblockdoormod.item.ModItems
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.screen.ModScreenHandlers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object FunnyBlockDoorMod : ModInitializer {

	const val MOD_ID = "funnyblockdoormod"

    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
		/*
		PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { world, player, pos, state, entity ->
			val serverState = world.server?.let { funnyDoorPersistantState.getServerState(it) }

			/*
			if (serverState != null) {
				if (serverState.protectedDoorBlocks.contains(pos)) {
					return@Before false
				}
			}
			*/
			serverState?.addBlock(pos)
			player.sendMessage(Text.of("${serverState?.getLength()}"), false)
			return@Before true
		})
		 */
		ServerPlayNetworking.registerGlobalReceiver(Identifier(MOD_ID, "update_depth_d"))
		{ server, player, handler, buf, responseSender ->
			val pos = buf.readBlockPos()
			val deltaDepth = buf.readInt()

			server.execute {
				// Update the depth variable on the server side
				val blockEntity = player.world.getBlockEntity(pos) as? doorEmitterBlockEntity ?: return@execute

				blockEntity.modifyInvDepth(deltaDepth)
			}
		}


		if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
			logger.info("Tech Reborn is loaded")
			doorEmitterBlockEntity.defaultEnergyBehaviourFactory = teamRebornEnergy.Companion
		} else {
			logger.info("Tech Reborn is not loaded")
			// Tech Reborn is not loaded, do not use its API
		}

		ModItems.registerItems()
		ModBlocks.registerModBlocks()
		ModItemGroups.registerItemGroups()
		ModScreenHandlers.registerScreenHandlers()
		ModBlockEntities.registerBlockEntities()

	}
}