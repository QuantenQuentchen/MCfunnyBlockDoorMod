package funnyblockdoormod.funnyblockdoormod.command

import com.mojang.brigadier.Command
import funnyblockdoormod.funnyblockdoormod.data.FFPermissions
import funnyblockdoormod.funnyblockdoormod.data.ForceField
import funnyblockdoormod.funnyblockdoormod.data.ffShape.Collapsible
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.Ownable
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.AABB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.*
import net.minecraft.command.argument.BlockPosArgumentType as BlockPosArg
import net.minecraft.server.command.CommandManager as CM


object Debug {
    private var debugField: ForceField? = null
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment->
            dispatcher.register(
                CM.literal("debug")
                    .then(CM.literal("createField").executes { ctx ->
                        val src = ctx.source
                        val player = src.player
                        runBlocking(Dispatchers.IO){
                            debugField = ForceField.getOrCreate(UUID.randomUUID())
                        }
                        val msg = Text.literal("Field created! With UUID: ${debugField?.uuid}")
                        player?.sendMessage(msg, false)
                        src.sendFeedback({ msg }, false)
                        Command.SINGLE_SUCCESS
                    }
                    )
                    .then(
                        CM.literal("grantAccess")
                            .then(
                                CM.argument("target", EntityArgumentType.player()).executes { ctx ->
                                    val src = ctx.source
                                    val target = EntityArgumentType.getPlayer(ctx, "target")
                                    val msg = Text.literal("Granting debug access to ${target.name.string}")
                                    debugField?.setPermission(target.uuid, FFPermissions.ALL)
                                    target.sendMessage(Text.literal("You were granted debug access"), false)
                                    src.sendFeedback({ msg }, false)
                                    Command.SINGLE_SUCCESS
                                }
                            )
                    )
                    .then(
                        CM.literal("revokeAccess")
                            .then(
                                CM.argument("target", EntityArgumentType.player()).executes { ctx ->
                                    val src = ctx.source
                                    val target = EntityArgumentType.getPlayer(ctx, "target")
                                    val msg = Text.literal("Revoking debug access to ${target.name.string}")
                                    debugField?.setPermission(target.uuid, FFPermissions.NONE)
                                    target.sendMessage(Text.literal("You lost debug access"), false)
                                    src.sendFeedback({ msg }, false)
                                    Command.SINGLE_SUCCESS
                                }
                            )
                    )
                    .then(
                        CM.literal("addField").then(
                                CM.argument("pos1", BlockPosArg.blockPos())
                                    .then(
                                    CM.argument("pos2", BlockPosArg.blockPos()).executes { ctx ->
                                        handleAddField(ctx.source, ctx)
                                        }
                                    )
                            )
                    )
                    .then(
                        CM.literal("serialization").executes { ctx ->
                            val result = SerializableTester.runAllTests()

                            val msg = Text.literal("Serialization tests: ${result.passed}/${result.total} passed")
                            val src = ctx.source
                            src.sendFeedback({ msg }, false)
                            Command.SINGLE_SUCCESS
                        }
                    )
                    .then(
                        CM.literal("addFieldSimple").executes { ctx -> handleAddFieldPlayer(ctx.source, ctx) }
                    )

            )
        }
    }
    private fun handleAddField(src: ServerCommandSource, ctx: com.mojang.brigadier.context.CommandContext<ServerCommandSource>): Int {
        val pos1 = BlockPosArg.getBlockPos(ctx, "pos1")
        val pos2 = BlockPosArg.getBlockPos(ctx, "pos2")
        val msg = Text.literal("addField from $pos1 to $pos2")
        val shape = AABB(pos1, pos2)
        val owner = Ownable(debugField!!, src.world)
        val collapsible = Collapsible(0.0)
        val newField = FFComponent(shape, owner, collapsible)
        debugField?.attemptVolumeAddition(newField, src.world)
        src.player?.sendMessage(msg, false)
        src.sendFeedback({ msg }, false)
        return Command.SINGLE_SUCCESS
    }
    private fun handleAddFieldPlayer(src: ServerCommandSource, ctx: com.mojang.brigadier.context.CommandContext<ServerCommandSource>): Int {
        val playerPos = src.player?.blockPos ?: return Command.SINGLE_SUCCESS
        val min = playerPos.add(-80, -80, -80)
        val max = playerPos.add(80, 80, 80)
        val msg = Text.literal("addField from $min to $max")
        val shape = AABB(min, max)
        val owner = Ownable(debugField!!, src.world)
        val collapsible = Collapsible(0.0)
        val newField = FFComponent(shape, owner, collapsible)
        debugField?.attemptVolumeAddition(newField, src.world)
        src.player?.sendMessage(msg, false)
        src.sendFeedback({ msg }, false)
        return Command.SINGLE_SUCCESS
    }
}