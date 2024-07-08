package funnyblockdoormod.funnyblockdoormod.screen.expandableScreen

import com.mojang.blaze3d.systems.RenderSystem
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.Vec2i
import funnyblockdoormod.funnyblockdoormod.core.containerClasses.imgPartInfo
import funnyblockdoormod.funnyblockdoormod.core.dataClasses.IntPoint2D
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.ButtonIcon
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ExpandableScreen(
    val parent: DoorEmitterScreen,
    private val handler: ScreenHandler,
    private val tabIdx: Int,
    private val extendedTab: ButtonIcon,
    private val collapsedTab: ButtonIcon,
    private val maxExtensionSteps: Int = 20,

    private val tabTexture: Identifier,

    private val cornerTopLeft: imgPartInfo,
    private val cornerTopRight: imgPartInfo,
    private val cornerBotRight: imgPartInfo,
    private val cornerBotLeft: imgPartInfo,

    private val borderTop: imgPartInfo,
    private val borderBot: imgPartInfo,
    private val borderLeft: imgPartInfo,
    private val borderRight: imgPartInfo
): PressableWidget(0, 0, 0, 0, null){

    enum class State {
        EXPANDED,
        EXPANDING,
        COLLAPSED,
        COLLAPSING
    }

    interface Factory {
        fun createScreen(parent: DoorEmitterScreen, handler: DoorEmitterScreenHandler, tabIdx: Int): ExpandableScreen
    }

    private val clickMap: Set<IntPoint2D> = collapsedTab.clickMap


    private val tabWidth: Int = extendedTab.textureWidth
    private val tabHeight: Int = extendedTab.textureHeight
    private val tabU = extendedTab.u
    private val tabV = extendedTab.v

    override fun getWidth(): Int {
        return tabWidth
    }

    override fun getHeight(): Int {
        return tabHeight
    }

    fun getHandler(): ScreenHandler {
        return handler
    }

    private var currentExtensionState: State = State.COLLAPSED
    private var currentExtension = 0

    private val tabWidthFactor = tabWidth.toFloat() / maxExtensionSteps
    private val tabHeightFactor = tabHeight.toFloat() / maxExtensionSteps

    init{
        parent.addSpacing(collapsedTab.textureHeight)
    }

    private fun setExpandingState(){
        if(currentExtensionState == State.EXPANDED) return
        currentExtensionState = State.EXPANDING
    }

    private fun setCollapsingState(){
        if(currentExtensionState == State.COLLAPSED) return
        currentExtensionState = State.COLLAPSING
    }

    private fun setExpandedState(){
        currentExtensionState = State.EXPANDED
    }

    private fun setCollapsedState(){
        currentExtensionState = State.COLLAPSED
    }

    private fun toggleTab(){

        when(currentExtensionState){
            State.EXPANDED -> setCollapsingState()
            State.COLLAPSED -> setExpandingState()
            else -> return
        }
    }

    private fun getCurrWidth(): Int {
        return min((currentExtension*tabWidthFactor).roundToInt(), tabWidth)
    }

    private fun getCurrHeight(): Int {
        return min((currentExtension*tabHeightFactor).roundToInt(), tabHeight)
    }

    fun drawTick(ctx: DrawContext){

        val localSpace = getLocalSpace()

        when(currentExtensionState){
            State.EXPANDING -> {
                expand()
                drawScreen(ctx, localSpace)
                overlayBorders(ctx, localSpace)
            }
            State.COLLAPSING -> {
                collapse()
                drawScreen(ctx, localSpace)
                overlayBorders(ctx, localSpace)
            }
            State.EXPANDED -> {
                drawExpanded(ctx, localSpace)
                whenExpandedInternal(ctx)
            }
            State.COLLAPSED -> {
                drawCollapsed(ctx, localSpace)
                whenCollapsedInternal(ctx)
            }
        }

        parent.modifySpacing(tabIdx, getCurrHeight())

    }

    fun getLocalSpace(): Vec2i{
        return parent.getTabsOrigin(tabIdx)
    }

    private fun drawScreen(ctx: DrawContext, localSpace: Vec2i) {
        ctx.drawTexture(tabTexture, localSpace.x, localSpace.y, tabU, tabV, getCurrWidth(), getCurrHeight())
    }

    private fun drawCollapsed(ctx: DrawContext, localSpace: Vec2i) {
        ctx.drawTexture(
            collapsedTab.identifier,
            localSpace.x, localSpace.y,
            collapsedTab.u, collapsedTab.v,
            collapsedTab.textureWidth, collapsedTab.textureHeight
        )
    }

    private fun drawExpanded(ctx: DrawContext, localSpace: Vec2i) {
        ctx.drawTexture(
            extendedTab.identifier,
            localSpace.x, localSpace.y,
            extendedTab.u, extendedTab.v,
            extendedTab.textureWidth, extendedTab.textureHeight
        )
    }

    private fun expand(){
        currentExtension++
        if(currentExtension > maxExtensionSteps) setExpandedState()
    }

    private fun collapse(){
        currentExtension--
        if(getCurrWidth() <= collapsedTab.textureWidth || getCurrHeight() <= collapsedTab.textureHeight || currentExtension < 0){
            setCollapsedState()
        }
    }

    private fun overlayBorders(ctx: DrawContext, localSpace: Vec2i){

        val extensionY = getCurrHeight()//getCurrWidth()
        val extensionX = getCurrWidth()//getCurrHeight()
        val extension = currentExtension
        val posX = localSpace.x
        val posY = localSpace.y

        ctx.drawTexture(tabTexture, posX, posY, cornerTopLeft.x, cornerTopLeft.y, cornerTopLeft.sizeX!!, cornerTopLeft.sizeY!!)
        ctx.drawTexture(tabTexture, posX, posY + extensionY-5, cornerBotLeft.x, cornerBotLeft.y, cornerBotLeft.sizeX!!, cornerBotLeft.sizeY!!)
        ctx.drawTexture(tabTexture, posX + extensionX - cornerBotRight.sizeX!!, posY + extensionY-5, cornerBotRight.x, cornerBotRight.y, cornerBotRight.sizeX, cornerBotRight.sizeY!!)
        ctx.drawTexture(tabTexture, posX + extensionX - cornerTopRight.sizeX!!, posY, cornerTopRight.x, cornerTopRight.y, cornerTopRight.sizeX, cornerTopRight.sizeY!!)

        ctx.drawTexture(tabTexture, posX+cornerTopLeft.sizeX, posY, borderTop.x, borderTop.y, borderTop.getExtension(extension), borderTop.sizeY!!)
        ctx.drawTexture(tabTexture, posX+cornerTopLeft.sizeX, posY+extensionY-1-4, borderBot.x, borderBot.y-1, borderBot.getExtension(extension), borderBot.sizeY!!)

        ctx.drawTexture(tabTexture, posX, posY+cornerTopLeft.sizeY, borderLeft.x, borderLeft.y, borderLeft.sizeX!!, borderLeft.getExtension(extension))
        ctx.drawTexture(tabTexture, posX+extensionX-4, posY+cornerTopLeft.sizeY, borderRight.x, borderRight.y, borderRight.sizeX!!, borderRight.getExtension(extension))

    }

    fun registerSlots() {
        //TODO("Not yet implemented")
    }

    private fun transformToLocalSpace(mouseX: Double, mouseY: Double): IntPoint2D {
        val localSpace = getLocalSpace()

        val localX = (mouseX - localSpace.x).toInt()
        val localY = (mouseY - localSpace.y).toInt()

        return IntPoint2D(localX, localY)
    }

    override fun clicked(mouseX: Double, mouseY: Double): Boolean {
        val localPoint = transformToLocalSpace(mouseX, mouseY)
        return clickMap.contains(localPoint)
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val localPoint = transformToLocalSpace(mouseX, mouseY)
        return clickMap.contains(localPoint)
    }

/*    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)
    }*/

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        super.mouseMoved(mouseX, mouseY)
        if (isMouseOver(mouseX, mouseY)) {
            whenOverlayedInternal()
            //mcInstance?.currentScreen?.renderTooltip(icon.identifier, mouseX.toInt(), mouseY.toInt())
        }
    }

    override fun onPress() {
        toggleTab()
        onPressedInternal()
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        super.onRelease(mouseX, mouseY)
        onReleasedInternal(isMouseOver(mouseX, mouseY))
    }

    private fun onPressedInternal() {
        //TODO("Not yet implemented")
        onPressed()
    }

    private fun whenCollapsedInternal(ctx: DrawContext) {
        //TODO("Not yet implemented")
        whenCollapsed(ctx)
    }

    private fun whenExpandedInternal(ctx: DrawContext) {
        //TODO("Not yet implemented")
        whenExpanded(ctx)
    }

    private fun whenOverlayedInternal() {
        //TODO("Not yet implemented")
        whenOverlayed()
    }

    private fun onReleasedInternal(isMouseOver: Boolean) {
        //TODO("Not yet implemented")
        onReleased(isMouseOver)
    }

    override fun renderButton(ctx: DrawContext,x: Int, y: Int, tickDelta: Float) {

        ctx.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()

        drawTick(ctx)

    }


    abstract fun whenExpanded(ctx: DrawContext)
    abstract fun whenCollapsed(ctx: DrawContext)
    abstract fun whenOverlayed()

    abstract fun getBlockedSlots(): List<Int>

    abstract fun onPressed()
    abstract fun onReleased(isMouseOver: Boolean)

}