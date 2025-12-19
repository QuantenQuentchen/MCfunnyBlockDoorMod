package funnyblockdoormod.funnyblockdoormod.ui.screens

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.MOD_ID
import funnyblockdoormod.funnyblockdoormod.data.FFPermission
import funnyblockdoormod.funnyblockdoormod.data.FFPermissions
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.screenhandler.ForceFieldScreenHandler
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen
import io.wispforest.owo.ui.base.BaseUIModelScreen
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.CheckboxComponent
import io.wispforest.owo.ui.container.FlowLayout
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.util.Base64

class ForceFieldScreen(
    handler: ForceFieldScreenHandler, inventory:PlayerInventory, title: Text,
    private val modelPath: String = "test_screen_model"
) : BaseUIModelHandledScreen<FlowLayout, ForceFieldScreenHandler>(
    handler, inventory, title, FlowLayout::class.java,
    BaseUIModelScreen.DataSource.asset(Identifier(MOD_ID, modelPath))
) {

    var scrollContainer: FlowLayout? = null

    val fields: MutableMap<UUID, FFComponent> = mutableMapOf()

    val permissions: MutableMap<UUID, FFPermissions> = mutableMapOf()

    private var filled: Boolean = false

    private fun setDisplayMode(mode: ScreenType) {
        if (mode == screenState) return
        if (scrollContainer == null) return
        screenState = mode
        scrollContainer?.clearChildren()
        filled = false
        when (mode) {
            ScreenType.FORCE_FIELD -> {
                for ((_, comp) in fields) {
                    addFFAreaEntry(comp)
                }
                screenState = ScreenType.FORCE_FIELD
            }

            ScreenType.PERMISSIONS -> {
                for ((uuid, perms) in permissions) {
                    addPlayerPermissionEntry(uuid, perms)
                }
                screenState = ScreenType.PERMISSIONS
            }

            else -> {}
        }
        filled = true
    }

    companion object {

        data class CacheEntry(val identifier: Identifier, val name: String)

        private val cache = ConcurrentHashMap<UUID, CacheEntry>()
        private val textureManager get() = MinecraftClient.getInstance().textureManager
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        /**
         * Gets or creates a face texture identifier for a player
         * Fetches from Mojang API if needed
         * @param playerUuid The player's UUID
         * @return An Identifier for the face texture, or null if extraction fails
         */
        fun getFaceTextureAsync(playerUuid: UUID, callback: (CacheEntry?) -> Unit) {
            // Check cache first
            cache[playerUuid]?.let {
                callback(it)
                return
            }

            scope.launch {
                try {
                    val faceId = fetchAndCreateFaceTexture(playerUuid)
                    withContext(Dispatchers.Main) {
                        callback(faceId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            }
        }

        /**
         * Synchronous version - blocks the thread, use sparingly
         */
        fun getFaceTexture(playerUuid: UUID): CacheEntry? {
            return cache.getOrPut(playerUuid) {
                runBlocking {
                    fetchAndCreateFaceTexture(playerUuid) ?: return@runBlocking null
                }
            }
        }

        /**
         * Fetches skin from Mojang API and creates face texture
         */
        private fun fetchAndCreateFaceTexture(playerUuid: UUID): CacheEntry? {
            // Format UUID without dashes for Mojang API
            val uuidString = playerUuid.toString().replace("-", "")

            // Fetch profile from Mojang
            val profileUrl = "https://sessionserver.mojang.com/session/minecraft/profile/$uuidString"
            val profileJson = URL(profileUrl).readText()
            val profile = Json.parseToJsonElement(profileJson).jsonObject

            // Decode the textures property (base64 encoded JSON)
            val texturesProperty = profile["properties"]?.jsonArray
                ?.firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.content == "textures" }
                ?.jsonObject?.get("value")?.jsonPrimitive?.content
                ?: return null

            val decodedTextures = String(Base64.getDecoder().decode(texturesProperty))
            val texturesJson = Json.parseToJsonElement(decodedTextures).jsonObject
            val skinUrl = texturesJson["textures"]?.jsonObject
                ?.get("SKIN")?.jsonObject
                ?.get("url")?.jsonPrimitive?.content
                ?: return null

            // Download the skin texture
            val skinImageData = URL(skinUrl).openStream().use { it.readBytes() }
            val skinImage = NativeImage.read(skinImageData.inputStream())

            // Create a new 8x8 image for the face
            val faceImage = NativeImage(8, 8, true)

            // Copy the face region (UV 8,8 to 16,16 on 64x64 texture)
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    val color = skinImage.getColor(8 + x, 8 + y)
                    faceImage.setColor(x, y, color)
                }
            }

            skinImage.close()

            // Register texture on main thread
            val faceId = Identifier("yourmod", "face/$playerUuid")
            //MinecraftClient.getInstance().executeSync {
            val dynamicTexture = NativeImageBackedTexture(faceImage)
            textureManager.registerTexture(faceId, dynamicTexture)
            //}

            val name = profile["name"]?.jsonPrimitive?.content ?: "Unknown"

            val entry = CacheEntry(faceId, name)

            cache[playerUuid] = entry
            return entry
        }

        /**
         * Clears the cache and destroys all registered textures
         */
        fun clearCache() {
            cache.values.forEach { faceId ->
                textureManager.getTexture(faceId.identifier)?.let { texture ->
                    textureManager.destroyTexture(faceId.identifier)
                    if (texture is NativeImageBackedTexture) {
                        texture.close()
                    }
                }
            }
            cache.clear()
        }

        /**
         * Removes a specific player's face texture from cache
         */
        fun removeFaceTexture(playerUuid: UUID) {
            cache.remove(playerUuid)?.let { faceId ->
                textureManager.getTexture(faceId.identifier)?.let { texture ->
                    textureManager.destroyTexture(faceId.identifier)
                    if (texture is NativeImageBackedTexture) {
                        texture.close()
                    }
                }
            }
        }

        /**
         * Cleanup coroutines
         */
        fun shutdown() {
            scope.cancel()
            clearCache()
        }
    }

    enum class ScreenType {
        FORCE_FIELD,
        PERMISSIONS,
        UNINITIALIZED
    }

    private var screenState = ScreenType.UNINITIALIZED

    private fun permissionCallback(checked: Boolean, uuid: UUID, perm: FFPermission){
        println("Permission set to $checked for $perm clicked for player $uuid")
    }

    private fun configurePermissionCallback(component: CheckboxComponent, uuid: UUID, perm: FFPermission, permissions: FFPermissions){
        component.onChanged { checked -> permissionCallback(checked, uuid, perm) }
        component.checked(permissions.has(perm))
    }

    enum class SwitchButtonType {
        SHOW, ACTIVE
    }

    private fun configureSwitchButton(component: ButtonComponent, uuid: UUID, type: SwitchButtonType){
        val ffRef = fields[uuid] ?: return
        val activationComp = ffRef.activatable ?: return
        val visualization = ffRef.visualization ?: return
        component.onPress { comp -> switchButtonCallback(comp, uuid, type) }

        when(type){
            SwitchButtonType.SHOW -> {
                component.active = !visualization.isVisible
            }
            SwitchButtonType.ACTIVE -> {
                component.active = !activationComp.isActive
            }
        }
        //component.active()
    }

    private fun switchButtonCallback(component: ButtonComponent, uuid: UUID, type: SwitchButtonType){
        val ffRef = fields[uuid] ?: return
        val activationComp = ffRef.activatable ?: return
        val visualization = ffRef.visualization ?: return
        when(type){
            SwitchButtonType.SHOW -> {
                visualization.isVisible = !visualization.isVisible
                component.active = !visualization.isVisible
            }
            SwitchButtonType.ACTIVE -> {
                activationComp.isActive = !activationComp.isActive
                component.active = !activationComp.isActive
            }
        }
    }

    private fun removePlayerCallback(uuid: UUID){
        if (screenState != ScreenType.PERMISSIONS) return
        println("Remove player $uuid clicked")
        val targetChild = scrollContainer?.childById(FlowLayout::class.java, "player-permission-entry-$uuid") ?: return
        scrollContainer?.removeChild(targetChild)
    }

    private fun removeAreaCallback(uuid: UUID){
        if (screenState != ScreenType.FORCE_FIELD) return
        println("Remove area $uuid clicked")
        val targetChild = scrollContainer?.childById(FlowLayout::class.java, "force-field-area-entry-$uuid") ?: return
        scrollContainer?.removeChild(targetChild)
    }

    private val checkBoxMapping = mapOf(
        FFPermission.BUILD to {uuid: UUID ->"build-checkbox-$uuid"},
        FFPermission.USE to {uuid: UUID -> "use-checkbox-$uuid"},
        FFPermission.BREAK to {uuid: UUID -> "break-checkbox-$uuid"},
        FFPermission.TELEPORT to {uuid: UUID -> "teleport-checkbox-$uuid"},
        FFPermission.ADMIN to {uuid: UUID -> "admin-checkbox-$uuid"}
    )

    private fun addPlayerPermissionEntry(uuid: UUID, perms: FFPermissions = FFPermissions.NONE) {
        if (screenState != ScreenType.PERMISSIONS) return
        val playerInfo = getFaceTexture(uuid) ?: return
        val template = this.model.expandTemplate(
            FlowLayout::class.java,
            "player-permission-template@$MOD_ID:$modelPath",
            mapOf(
                "player-name" to playerInfo.name,
                "player-uuid" to uuid.toString(),
                "player-texture" to playerInfo.identifier.toString()
            )
        )
        for ((permission, buttonID) in checkBoxMapping){
            template.childById(CheckboxComponent::class.java, buttonID(uuid))?.configure { comp: CheckboxComponent ->
                configurePermissionCallback(comp, uuid, permission, perms)
            }
        }
        template.childById(ButtonComponent::class.java, "remove-player-button-$uuid")?.onPress { buttonComponent ->
            buttonComponent.onPress { _ -> removePlayerCallback(uuid) }
        }
        scrollContainer?.child(template)
    }

    private fun addFFAreaEntry(comp: FFComponent){
        if (screenState != ScreenType.FORCE_FIELD) return
        val template = this.model.expandTemplate(
            FlowLayout::class.java,
            "force-field-area-template@$MOD_ID:$modelPath",
            mapOf(
                "area-uuid" to comp.ownership?.uuid.toString(),
                "area-name" to comp.shape.toUIString()
            )
        )
        template.childById(ButtonComponent::class.java, "activate-area-button-${comp.ownership?.uuid}")?.configure { component: ButtonComponent ->
            configureSwitchButton(component, comp.ownership?.uuid ?: return@configure, SwitchButtonType.ACTIVE)
        }
        template.childById(ButtonComponent::class.java, "show-area-button-${comp.ownership?.uuid}")?.configure { component: ButtonComponent ->
            configureSwitchButton(component, comp.ownership?.uuid ?: return@configure, SwitchButtonType.SHOW)
        }
        template.childById(ButtonComponent::class.java, "remove-area-button-${comp.ownership?.uuid}")?.onPress { buttonComponent ->
            buttonComponent.onPress { _ -> removeAreaCallback(comp.ownership?.uuid ?: return@onPress) }
        }
        scrollContainer?.child(template)
    }

    override fun init() {
        super.init()
        if (this.uiAdapter == null) return
        setDisplayMode(screenState)
    }

    override fun build(rootComponent: FlowLayout?) {
        rootComponent?.childById(FlowLayout::class.java, "scroll-container")?.let { scrollContainer ->
            this.scrollContainer = scrollContainer
        }
        //TODO("Not yet implemented")
    }
}
