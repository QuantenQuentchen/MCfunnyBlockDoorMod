package funnyblockdoormod.funnyblockdoormod

import funnyblockdoormod.funnyblockdoormod.block.ModBlocks
import funnyblockdoormod.funnyblockdoormod.item.ModItemGroups
import funnyblockdoormod.funnyblockdoormod.item.ModItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
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
		ModItems.registerItems()
		ModBlocks.registerModBlocks()
		ModItemGroups.registerItemGroups()


	}
}