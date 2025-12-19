package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import funnyblockdoormod.funnyblockdoormod.block.entitiy.ModBlockEntities
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.WirelessRedstoneState
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.baseWirelessRedstone
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.implementations.teamRebornEnergy
import funnyblockdoormod.funnyblockdoormod.block.entitiy.behaviour.interfaces.IchangableChannel
import funnyblockdoormod.funnyblockdoormod.block.entitiy.DoorEmitterBlockEntity
import funnyblockdoormod.funnyblockdoormod.command.Commands
import funnyblockdoormod.funnyblockdoormod.config.WorldTweakConfig
import funnyblockdoormod.funnyblockdoormod.data.ServerHolder
import funnyblockdoormod.funnyblockdoormod.events.EventHandler
import funnyblockdoormod.funnyblockdoormod.item.ModItemGroups
import funnyblockdoormod.funnyblockdoormod.item.ModItems
import funnyblockdoormod.funnyblockdoormod.screenhandler.ModScreenHandlers
import funnyblockdoormod.funnyblockdoormod.screenhandler.WirelessRedstoneScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object FunnyBlockDoorMod : ModInitializer {

    @JvmField
    var CONFIG = WorldTweakConfig.createAndLoad()

	const val MOD_ID = "funnyblockdoormod"

    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {

        val test = Identifier(MOD_ID, "test_screen_model")

		logger.info("Hello Fabric world!")

        ServerLifecycleEvents.SERVER_STARTED.register { srv: MinecraftServer -> ServerHolder.server = srv }
        ServerLifecycleEvents.SERVER_STOPPED.register { srv: MinecraftServer ->
            if (ServerHolder.server === srv) ServerHolder.server = null
        }

        ServerPlayNetworking.registerGlobalReceiver(WirelessRedstoneScreenHandler.CHANNEL_STATE_UPDATE_PACKAGE)
		{ server, player, handler, buf, responseSender ->
			val pos = buf.readBlockPos()
			val channel = buf.readInt()

			// Perform some action based on the received data
			// This code will be executed on the server thread, so you can safely interact with the world here
			server.execute {
				val world = player.world
				val block = world.getBlockState(pos).block
				if (block is IchangableChannel) {
					block.setChannelState(world, pos, channel)
				}
			}
		}

		ServerLifecycleEvents.SERVER_STARTED.register { server ->
			val world = server.overworld
			logger.info("Loading persistent state")
			baseWirelessRedstone.wirelessRedstoneState = world.persistentStateManager.getOrCreate(
				{ nbt -> WirelessRedstoneState(nbt) }, // Read function
				{ WirelessRedstoneState(null) }, // Supplier
				"WirelessRedstoneState" // Data file name
			)
		}

		ServerPlayNetworking.registerGlobalReceiver(Identifier(MOD_ID, "update_depth_d"))
		{ server, player, handler, buf, responseSender ->
			val pos = buf.readBlockPos()
			val deltaDepth = buf.readInt()

			server.execute {
				// Update the depth variable on the server side
				val blockEntity = player.world.getBlockEntity(pos) as? DoorEmitterBlockEntity ?: return@execute

				blockEntity.modifyInvDepth(deltaDepth)
			}
		}


		if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
			logger.info("Tech Reborn is loaded")
			DoorEmitterBlockEntity.defaultEnergyBehaviourFactory = teamRebornEnergy.Companion
		} else {
			logger.info("Tech Reborn is not loaded")
			// Tech Reborn is not loaded, do not use its API
		}

		ModItems.registerItems()
		ModBlocks.registerModBlocks()
		ModItemGroups.registerItemGroups()
		ModScreenHandlers.registerScreenHandlers()
		ModBlockEntities.registerBlockEntities()
        EventHandler.registerEventListeners()
        Commands.registerCommands()

	}
}