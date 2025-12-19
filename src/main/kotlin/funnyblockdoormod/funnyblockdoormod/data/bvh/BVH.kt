package funnyblockdoormod.funnyblockdoormod.data.bvh

import funnyblockdoormod.funnyblockdoormod.core.containerClasses.OverlapScalar
import funnyblockdoormod.funnyblockdoormod.data.ForceField
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape
import funnyblockdoormod.funnyblockdoormod.serialize.Serializable
import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.UUID
import kotlin.collections.ArrayDeque

class BVH(private val region: RegionPos, private val world: RegistryKey<World>): Serializable(){//: PersistentState() {
    var root: BVHNode? = null
    var size: Int = 0

    override fun writeNBT(): NbtElement {
        return serializeToNBT()
    }

    override fun getKey(): String {
        val highUL = region.x.toULong()
        val lowUL = region.y.toULong()
        return ((highUL shl 32) or lowUL).toString()
    }

    override fun getRegion(): String {
        return REGION_PREFIX + world.value.toString()
    }

    private val pointQueryStack = ArrayDeque<BVHNode>()
    private val intersectQueryStack = ArrayDeque<BVHNode>()
    private val allVolumes = ArrayList<FFComponent>()
    private val nodeByVolume = HashMap<FFComponent, BVHNode>() //Could be consolidated into the BVHNode
    private val fieldByUUID = HashMap<UUID, FFComponent>()

    companion object {
        private const val REGION_PREFIX = "ff_region"
        private fun regionFor(world: ServerWorld) = REGION_PREFIX + world.registryKey.value.toString()
        private fun regionFrom(key: String): RegistryKey<World>? = key.substring(REGION_PREFIX.length).let {
            RegistryKey.of(
                RegistryKeys.WORLD,
                net.minecraft.util.Identifier.tryParse(it)!!
            )
        }

        private fun keyFor(region: RegionPos) = ((region.x.toULong() shl 32) or region.y.toULong()).toString()
        private fun regionPosFrom(key: String): RegionPos {
            val ulongKey = key.toULong()
            val x = (ulongKey shr 32).toInt()
            val y = (ulongKey and 0xFFFFFFFFu).toInt()
            return RegionPos(x, y)
        }

        fun getBVH(world: ServerWorld, region: RegionPos): BVH? {
            return runBlocking(Dispatchers.IO) {
                Serializable.get(
                    Serializable.Companion.RootRef.HARD,
                    regionFor(world), keyFor(region)
                ) { nbt: NbtElement, regionStr: String, keyStr: String ->
                    fromNbt(nbt as NbtCompound, regionStr, keyStr.toULong())
                } as BVH?
            }
        }

        fun getOrCreateBVH(world: ServerWorld, region: RegionPos): BVH {
            val worldRegistryKey = world.registryKey
            return runBlocking(Dispatchers.IO) {
                Serializable.getOrCreate(
                    Serializable.Companion.RootRef.HARD,
                    regionFor(world), keyFor(region),
                    { nbt: NbtElement, regionStr: String, keyStr: String ->
                        fromNbt(
                            nbt as NbtCompound,
                            regionStr,
                            keyStr.toULong()
                        )
                    },
                    { BVH(region, worldRegistryKey) }
                ) as BVH
            }
        }

        private fun fromNbt(nbt: NbtCompound, region: String, key: ULong): BVH {
            return BVH(nbt, region, key)
        }
    }

    private fun traverseNode(node: BVHNode, pos: BlockPos): BVHNode? {
        pointQueryStack.clear()
        pointQueryStack.addLast(node)
        while (pointQueryStack.isNotEmpty()) {
            val current = pointQueryStack.removeLast()
            if (current.containsPoint(pos)) return current
            if (current.isLeaf) continue
            current.right?.let { pointQueryStack.addLast(it) }
            current.left?.let { pointQueryStack.addLast(it) }
        }
        return null
    }

    private inline fun traverseIntersection(
        other: IShape, crossinline nodeOperation: (BVHNode) -> Unit = {},
    ) {

        if (root == null) return
        intersectQueryStack.clear()
        intersectQueryStack.addLast(root!!)

        while (intersectQueryStack.isNotEmpty()) {
            val current = intersectQueryStack.removeLast()

            if (!current.intersectsBroad(other)) continue

            if (current.isLeaf && current.boundingBox != null) {
                nodeOperation(current)
            } else {
                // push right then left so left is processed first (LIFO)
                current.right?.let { intersectQueryStack.addLast(it) }
                current.left?.let { intersectQueryStack.addLast(it) }
            }
        }
        return
    }

    private fun buildBVH(volumes: List<FFComponent>): BVHNode? {
        if (volumes.isEmpty()) return null
        if (volumes.size == 1) {
            val leaf = BVHNode().apply { boundingBox = volumes[0]; isLeaf = true }
            allVolumes.add(volumes[0]) // add leaf to cached list at build time
            nodeByVolume[volumes[0]] = leaf
            volumes[0].ownership?.let {
                fieldByUUID[it.uuid] = volumes[0]
            }
            return leaf
        }
        val sorted = volumes.sortedBy { it.shape.getBoundingBox().centerI.x } // simple X-axis split
        val mid = sorted.size / 2
        return BVHNode().apply {
            left = buildBVH(sorted.subList(0, mid)).also{ it?.parent = this }
            right = buildBVH(sorted.subList(mid, sorted.size)).also{ it?.parent = this }
            // safe non-null after recursion
            boundingBox = BVHNode.mergeBounds(left!!.boundingBox!!, right!!.boundingBox!!)
        }
    }

    private fun areaVolume(area: FFComponent): Double {
        // use intersection with itself to get full volume (implementation-dependent but commonly supported)
        return area.shape.volume//.intersectionVolume(area)
    }

    private fun chooseInsertionSibling(leafArea: FFComponent): BVHNode {
        var current = root!!
        while (!current.isLeaf) {
            val leftNode = current.left
            val rightNode = current.right
            if (leftNode == null) {
                current = rightNode!!
                continue
            }
            if (rightNode == null) {
                current = leftNode
                continue
            }
            // cost = unionVolume(child, leaf) - childVolume
            val leftUnion = BVHNode.mergeBounds(leftNode.boundingBox!!, leafArea)
            val rightUnion = BVHNode.mergeBounds(rightNode.boundingBox!!, leafArea)
            val leftIncrease = areaVolume(leftUnion) - areaVolume(leftNode.boundingBox!!)
            val rightIncrease = areaVolume(rightUnion) - areaVolume(rightNode.boundingBox!!)
            current = if (leftIncrease <= rightIncrease) leftNode else rightNode
        }
        return current
    }

    /**
     * Insert area into BVH using heuristic descent (minimal bounding increase).
     * O(height) allocation-minimal: only creates one new internal node and one leaf.
     * Marks ancestors dirty so bounds are updated lazily.
     */
    fun addArea(area: FFComponent): Boolean {
        val success = addAreaInternal(area)
        if (success) { markDirty() }
        return success
    }

    private fun addAreaInternal(area: FFComponent): Boolean {
        if (nodeByVolume.containsKey(area)) return false

        val leaf = BVHNode().apply {
            boundingBox = area
            isLeaf = true
        }

        allVolumes.add(area)
        nodeByVolume[area] = leaf
        area.ownership?.let { fieldByUUID[it.uuid] = area }
        size++

        if (root == null) {
            root = leaf
            return true
        }

        // Find best sibling by descent heuristic
        val sibling = chooseInsertionSibling(area)

        // Create new parent that pairs sibling and new leaf
        val parent = BVHNode().apply {
            left = sibling
            right = leaf
            left?.parent = this
            right?.parent = this
            // set bounding box now to something valid; mark dirty so it will be recomputed when needed
            boundingBox = BVHNode.mergeBounds(sibling.boundingBox!!, leaf.boundingBox!!)
        }

        val grand = sibling.parent
        if (grand == null) {
            // sibling was root; new parent becomes root
            parent.parent = null
            root = parent
        } else {
            // replace sibling with new parent in grandparent
            if (grand.left === sibling) grand.left = parent else grand.right = parent
            parent.parent = grand
        }

        // mark parent and ancestors dirty (lazy recomputation)
        BVHNode.setDirty(parent)
        return true
    }

    /**
     * Remove area from BVH. Splices out its parent and reconnects sibling.
     * Marks ancestors dirty for lazy bounds updates.
     */
    fun removeArea(area: FFComponent): Boolean {
        val success = removeAreaInternal(area)
        if (success && size <= 0) {
            deleteSelf()
        }
        if (success) { markDirty() }
        return success
    }

    private fun removeAreaInternal(area: FFComponent): Boolean {
        val node = nodeByVolume.remove(area) ?: return false

        allVolumes.remove(area)
        area.ownership?.let { fieldByUUID.remove(it.uuid) }
        size--

        val parent = node.parent
        if (parent == null) {
            // node is root
            root = null
            return true
        }

        val sibling = if (parent.left === node) parent.right else parent.left

        if (sibling == null) {
            // Defensive: remove parent and attach nothing to grandparent
            val grand = parent.parent
            if (grand == null) {
                root = null
            } else {
                if (grand.left === parent) grand.left = null else grand.right = null
                BVHNode.setDirty(grand)
            }
            return true
        }

        val grand = parent.parent
        if (grand == null) {
            // parent was root -> sibling becomes new root
            sibling.parent = null
            root = sibling
        } else {
            // replace parent with sibling in grandparent
            if (grand.left === parent) grand.left = sibling else grand.right = sibling
            sibling.parent = grand
            BVHNode.setDirty(grand)
        }

        // ensure removed nodes are not referenced
        parent.left = null
        parent.right = null

        return true
    }

    fun getAllIntersections(other: FFComponent): ArrayList<FFComponent> {
        val intersections = ArrayList<FFComponent>(size)
        traverseIntersection(other.shape) { intersections.add(it.boundingBox!!) }
        return intersections
    }

    fun getAllHostileIntersections(other: IShape, owner: ForceField): ArrayList<FFComponent>{
        val intersections = ArrayList<FFComponent>()
        traverseIntersection(other) {
            val intersectionOwner = it.boundingBox!!.ownership?.owner ?: return@traverseIntersection
            if (intersectionOwner != owner) intersections.add(it.boundingBox!!)
        }
        return intersections
    }

    fun getOverlapScalar(other: IShape): HashMap<FFComponent, OverlapScalar> {
        val overlaps = HashMap<FFComponent, OverlapScalar>()
        traverseIntersection(other) {
            val overlapScalar = it.boundingBox!!.shape.intersectionVolume(other)
            val shrinkVector = it.boundingBox!!.shape.computeOwnShrinkVector(other)
            overlaps[it.boundingBox!!] = OverlapScalar(overlapScalar, shrinkVector)
        }
        return overlaps
    }

    fun getOverlapScalarTotal(other: IShape): Pair<HashMap<FFComponent, OverlapScalar>, Double> {
        val overlaps = HashMap<FFComponent, OverlapScalar>()
        var maxVolume = 0.0
        traverseIntersection(other) {
            val overlapScalar = it.boundingBox!!.shape.intersectionVolume(other)
            val shrinkVector = it.boundingBox!!.shape.computeOwnShrinkVector(other)
            overlaps[it.boundingBox!!] = OverlapScalar(overlapScalar, shrinkVector)
            maxVolume += overlapScalar
        }
        return Pair(overlaps, maxVolume)
    }

    fun getVolume(pos: BlockPos): FFComponent? {
        if (root == null) return null
        val node = traverseNode(root!!, pos)
        return node?.boundingBox
    }

    fun serializeToNBT(): NbtCompound {
        val forceFieldList = NbtList()
        for (forceField in allVolumes) {
            forceField.ownership?.uuid ?: continue
            val tag = NbtCompound()
            tag.putUuid("uuid", forceField.ownership.uuid)
            forceFieldList.add(tag)
        }
        val nbt = NbtCompound()
        nbt.put("forceFields", forceFieldList)
        nbt.putInt("size", size)
        return nbt
    }

    fun getForceField(uuid: UUID): FFComponent? {
        return fieldByUUID[uuid]
    }


    constructor(nbt: NbtCompound, regionKey: String, key: ULong) : this(regionPosFrom(key.toString()), regionFrom(regionKey)!!) {
        allVolumes.clear()
        val size = nbt.getInt("size")
        allVolumes.ensureCapacity(size)

        val compounds = nbt.getList("forceFields", NbtElement.COMPOUND_TYPE.toInt())
            .filterIsInstance<NbtCompound>()

        val vols: List<FFComponent> = runBlocking {
            // launch async coroutines on IO for each UUID
            compounds.map { compound ->
                async(Dispatchers.IO) {
                    val forceFieldUUID = compound.getUuid("uuid") ?: return@async null
                    FFComponent.getFromUUID(forceFieldUUID.toString()) // suspend call
                }
            }.awaitAll() // wait for all parallel results
        }.filterNotNull() // remove failed deserializations

        root = buildBVH(vols) // sequentially
        this@BVH.size = allVolumes.size
    }

/*    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        return serializeToNBT()
    }*/
}