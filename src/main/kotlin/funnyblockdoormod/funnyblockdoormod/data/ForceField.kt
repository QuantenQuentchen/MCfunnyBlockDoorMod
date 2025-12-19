package funnyblockdoormod.funnyblockdoormod.data

import funnyblockdoormod.funnyblockdoormod.FunnyBlockDoorMod.CONFIG
import funnyblockdoormod.funnyblockdoormod.block.entitiy.ForceFieldBlockEntity
import funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions.UUIDConst
import funnyblockdoormod.funnyblockdoormod.data.bvh.BVH
import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape
import funnyblockdoormod.funnyblockdoormod.serialize.Serializable
import funnyblockdoormod.funnyblockdoormod.utils.RegionPos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import java.util.UUID
import kotlin.math.floor
import kotlin.math.min

class ForceField(val uuid: UUID = UUID.randomUUID()): Serializable() {

    companion object{

        private const val REGION_KEY = "ForceFields"

        fun get(uuid: UUID): ForceField? {
            return runBlocking(Dispatchers.IO) {
                get(Serializable.Companion.RootRef.HARD, REGION_KEY, uuid.toString()) { tag, region, key ->
                    fromNbt(tag as NbtCompound)
                } as? ForceField
            }
        }

        suspend fun getOrCreate(uuid: UUID): ForceField {
            return getOrCreate(Serializable.Companion.RootRef.HARD,
                    REGION_KEY, uuid.toString(), {
                        tag, region, key -> fromNbt(tag as NbtCompound)
                    },
                    { ForceField(uuid)}
                ) as ForceField

        }

        fun fromNbt(nbt: NbtCompound): ForceField {
            return runBlocking(Dispatchers.IO) {
                val uuid = nbt.getUuid("uuid")
                val blockEntityPosX = nbt.getInt("blockEntityPosX")
                val blockEntityPosY = nbt.getInt("blockEntityPosY")
                val blockEntityPosZ = nbt.getInt("blockEntityPosZ")
                val dimensionIdentifier = Identifier.tryParse(nbt.getString("dimensionIdentifier"))
                val permissions = mutableMapOf<UUID, FFPermissions>()
                val permissionsList = nbt.getList("permissions", NbtElement.COMPOUND_TYPE.toInt())
                for (permissionNbt in permissionsList) {
                    if (permissionNbt !is NbtCompound) continue
                    val permissionUUID = permissionNbt.getUuid("playerUUID")
                    val permission = permissionNbt.getLong("permission").toULong()
                    permissions[permissionUUID] = FFPermissions.fromBits(permission)
                }
                val attacks = arrayListOf<Attack>()
                val attacksList = nbt.getList("attacks", NbtElement.COMPOUND_TYPE.toInt())
                for (attackNbt in attacksList) {
                    if (attackNbt !is NbtCompound) continue
                    val attack = Attack.fromNbt(attackNbt) ?: continue
                    attacks.add(attack)
                }
                return@runBlocking ForceField(uuid).apply {
                    this.blockEntityPos = BlockPos(blockEntityPosX, blockEntityPosY, blockEntityPosZ)
                    this.dimensionIdentifier = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier)
                    this.permissions.putAll(permissions)
                    this.attacks.addAll(attacks)
                }
            }
        }

    }

    constructor(): this(UUID.randomUUID()){
        markDirty()
    }

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putUuid("uuid", uuid)
        nbt.putInt("blockEntityPosX", blockEntityPos?.x ?: -1)
        nbt.putInt("blockEntityPosY", blockEntityPos?.y ?: -1)
        nbt.putInt("blockEntityPosZ", blockEntityPos?.z ?: -1)
        nbt.putString("dimensionIdentifier", dimensionIdentifier?.value.toString())
        val permissionsList = NbtList()
        for ((key, value) in permissions){
            val permissionNbt = NbtCompound()
            permissionNbt.putUuid("playerUUID", key)
            permissionNbt.putLong("permission", value.toULong().toLong())
            permissionsList.add(permissionNbt)
        }
        nbt.put("permissions", permissionsList)
        val attacksList = NbtList()
        for (attack in attacks){
            attacksList.add(attack.toNbt())
        }
        nbt.put("attacks", attacksList)
        return nbt
    }

    //Holds hydratable Reference to FF BlockEntity.

    //Constructed on demand by BVH loading.

    //Should be able to be serialized, probably in a cache owned globally or some shit.

    //In theory this would allow easy transfer of field configuration between worlds, and emitters.

    //Actually, now that I think of it, this should hold most custom state

    //Store world here for possible inter Dimensional shenanigans.
    data class Attack(
        var escallationLevel: Int = 0, val yieldThreshold: Double = 50.0,
        val targets: MutableSet<Target>, val successor: FFComponent,
        var state: AttackState = AttackState.IN_PROGRESS//, val worldKey: RegistryKey<World>
    ){
        enum class AttackState{
            SUCCESSFUL, FAILED, IN_PROGRESS
        }
        companion object {
            suspend fun fromNbt(nbt: NbtCompound): Attack?{
                val escallationLevel = nbt.getInt("escallationLevel")
                val yieldThreshold = nbt.getDouble("yieldThreshold")
                val successor = FFComponent.deserializeFromNbt(nbt.getCompound("successor")) ?: return null
                val state = AttackState.entries[nbt.getInt("state")]
                val targets = mutableSetOf<Target>()
                for (target in nbt.getList("targets", 10)){
                    targets.add(Target.fromNbt(target as NbtCompound) ?: continue)
                }
                return Attack(escallationLevel, yieldThreshold, targets, successor, state)
            }
        }

        fun toNbt(): NbtCompound{
            val nbt = NbtCompound()
            nbt.putInt("escallationLevel", escallationLevel)
            nbt.putDouble("yieldThreshold", yieldThreshold)
            val nbtTargets = NbtList()
            for (target in targets){
                nbtTargets.add(target.toNbt())
            }
            nbt.put("targets", nbtTargets)
            nbt.put("successor", successor.serializeToNbt(NbtCompound()))
            nbt.putInt("state", state.ordinal)
            return nbt
        }

        data class Target(val target: FFComponent, val owner: ForceField, var attackVec: Vec3d){
            companion object {
                suspend fun fromNbt(nbt: NbtCompound): Target?{
                    val targetUUID = nbt.getUuid("targetUUID")
                    val ownerUUID = nbt.getUuid("ownerUUID")
                    val attackVec = Vec3d(
                        nbt.getDouble("attackX"),
                        nbt.getDouble("attackY"),
                        nbt.getDouble("attackZ")
                    )
                    val target: FFComponent? = FFComponent.getFromUUID(targetUUID.toString())
                    val owner = ForceField.get(ownerUUID) ?: return null
                    if (target == null) {return null}
                    return Target(target, owner, attackVec)
                }
            }
            fun toNbt(): NbtCompound{
                val nbt = NbtCompound()
                nbt.putUuid("targetUUID", target.ownership?.uuid ?: UUIDConst.invalid())
                nbt.putUuid("ownerUUID", owner.uuid)
                nbt.putDouble("attackX", attackVec.x)
                nbt.putDouble("attackY", attackVec.y)
                nbt.putDouble("attackZ", attackVec.z)
                return nbt
            }
        }
    }

    data class FFComponentContainer(var component: FFComponent, var active: Boolean, var name: String = "New FinalFinalV3Field(3).docx")

    private val attacks: ArrayList<Attack> = arrayListOf()
    private val components: ArrayList<FFComponentContainer> = arrayListOf()


    private var _blockEntity: ForceFieldBlockEntity? = null
    var blockEntity: ForceFieldBlockEntity?
        get() {
            //Only works if chunk is loaded.
            if (_blockEntity == null) {
                val pos = blockEntityPos ?: return null
                val dim = dimensionIdentifier ?: return null
                val server = ServerHolder.server ?: return null
                val world = server.getWorld(dim)
                _blockEntity = world?.getBlockEntity(pos) as? ForceFieldBlockEntity ?: return null
            }
            return _blockEntity
        }
        set(value) {_blockEntity = value}

    var blockEntityPos: BlockPos? = null
    var dimensionIdentifier: RegistryKey<World?>? = null

    enum class FieldStrain {
        NONE, LOW, MEDIUM, HIGH
    }

    val permissions: MutableMap<UUID, FFPermissions> = mutableMapOf()

    var defaultPermission: FFPermissions = FFPermissions.NONE

    fun getPermission(playerUUID: UUID): FFPermissions {
        if (permissions.containsKey(playerUUID)) return permissions[playerUUID]!!
        return defaultPermission
    }

    fun setPermission(playerUUID: UUID, permission: FFPermissions) {
        permissions[playerUUID] = permission
        markDirty()
    }

    fun removePlayer(playerUUID: UUID) {
        permissions.remove(playerUUID)
        markDirty()
    }

    fun hasPlayer(playerUUID: UUID): Boolean {
        return permissions.containsKey(playerUUID)
    }

    fun clear() {
        permissions.clear()
    }

    //return true if defeated
    fun attackField(damage: Int, victim: FFComponent, pos: BlockPos, world: ServerWorld): Boolean {
        val attackVector = victim.shape.getShrinkVectorFromBlockHit(pos)
        return attackField(damage, victim, attackVector, world)
    }

    //return true if defeated
    fun attackField(damage: Int, victim: FFComponent, attackVector: Vec3d, world: ServerWorld): Boolean {
/*      TODO: This
        if (blockEntity == null){
            removeVolume(victim, world)
            return true
        }*/
        if (victim.ownership == null || victim.collapsible == null) throw (IllegalStateException("Victim Force Field Component is missing ownership or collapsible data.")) //return false
        val threshold = victim.collapsible.threshold ?: 0.0
        val victimUUID: UUID = (victim.ownership.uuid ?: UUIDConst.invalid())

        val attackPercentageRaw =
            victim.shape.shrinkVectorPercentage(attackVector) ?: CONFIG.damageDampeningMax()

        val dampeningValue =
            if (CONFIG.activateDamageDampening()) min(attackPercentageRaw, CONFIG.damageDampeningMax())
            else 1.0

        val volumeScale =
            if (CONFIG.activateVolumeDamageScale()) min(victim.shape.volume*CONFIG.volumeScale(), CONFIG.volumeScaleClamp())
            else 1.0

        val damage = floor((damage*volumeScale*dampeningValue)).toInt()
        world.players[0].sendMessage(
            Text.literal("Field $victimUUID is taking ${damage}, with Vector ${attackVector?.x}, ${attackVector?.y}, ${attackVector?.z}"), false)
        return false
        /*blockEntity!!.energy -= damage
        if (blockEntity!!.getPercentage() <= threshold){
            if (threshold == 0.0){
                removeVolume(victim, world)
                return true
            } else {
                val shrunkVolume = victim.shrinkByVector(attackVector)
                if (shrunkVolume != null) {
                    shrinkVolume(victim, shrunkVolume, world)
                    return true
                }
                removeVolume(victim, world)
                return true
            }
        }
        return false*/
    }

    //Should only be evoked from BlockEntity
    //Technically a world ref can be gotten here, for interdimensional shenanigans. You shouldn't but it is possible
    fun attackTick(world: ServerWorld){
        //Checking this anyway...
        if (blockEntity == null || attacks.isEmpty()) return
        val destroyedField: MutableSet<Attack.Target> = mutableSetOf()
        for(stagedAttack in attacks){
            if (stagedAttack.state != Attack.AttackState.IN_PROGRESS) continue
            val attackValue = CONFIG.attackEscalationRatio()*stagedAttack.escallationLevel
            destroyedField.clear()
            for (target in stagedAttack.targets){
                blockEntity!!.energy -= attackValue
                if (blockEntity!!.getPercentage() <= stagedAttack.yieldThreshold){
                    stagedAttack.state = Attack.AttackState.FAILED
                    break
                }
                val successful = target.owner.attackField(attackValue, target.target, target.attackVec, world)
                if (successful){
                    destroyedField.add(target)
                }
            }
            stagedAttack.escallationLevel++
            for (target in destroyedField){
                stagedAttack.targets.remove(target)
            }
            if (stagedAttack.targets.isEmpty()){
                attemptVolumeAddition(stagedAttack.successor, world)
                stagedAttack.state = Attack.AttackState.SUCCESSFUL
            }
        }
        markDirty()
    }

    fun attemptVolumeAddition(volume: FFComponent, world: ServerWorld){
        //if (blockEntity == null) return TODO: This
        val enemies: MutableSet<Attack.Target> = mutableSetOf()
        val enemyField = getProspectiveEnemies(volume.shape, world)
        for (enemy in enemyField){
            val owner = enemy.ownership?.owner ?: continue
            enemies.add(Attack.Target(enemy, owner, enemy.shape.computeOwnShrinkVector(volume.shape)))
        }
        if (enemies.isEmpty()){
            addVolume(volume, world)
            return
        }
        val attack = Attack(targets = enemies, successor = volume)
        attacks.add(attack)
        markDirty()
    }

    private fun getProspectiveEnemies(volume: IShape, world: ServerWorld): MutableSet<FFComponent>{
        val enemies = mutableSetOf<FFComponent>()
        for (regions in volume.getCoveredRegions()){
            val bvh = BVH.getBVH(world, regions)
            bvh?.getAllHostileIntersections(volume, this)?.let { enemies.addAll(it) }
        }
        return enemies
    }

    private fun removeVolume(volume: FFComponent, world: ServerWorld) {
        for (regions in volume.shape.getCoveredRegions()){
            val bvh = BVH.getBVH(world, regions)
            bvh?.removeArea(volume)
        }
        val comp = components.firstOrNull { it.component == volume } ?: return
        comp.active = false
    }

    private fun addVolume(volume: FFComponent, world: ServerWorld) {
        for (regions in volume.shape.getCoveredRegions()){
            val bvh = BVH.getOrCreateBVH(world, regions)
            bvh.addArea(volume)
        }
        components.add(FFComponentContainer(volume, true))
    }

    private fun shrinkVolume(oldVolume: FFComponent, newVolume: FFComponent, world: ServerWorld) {
        for (regions in oldVolume.shape.getCoveredRegions()){
            val bvh = BVH.getBVH(world, regions)
            bvh?.removeArea(oldVolume)
        }
        for (regions in newVolume.shape.getCoveredRegions()){
            val bvh = BVH.getOrCreateBVH(world, regions)
            bvh.addArea(newVolume)
        }
        val comp = components.firstOrNull { it.component == oldVolume } ?: return
        val oldComp = comp.component
        comp.component = newVolume
        oldComp.deleteSelf()
    }

    private fun fullyDeleteComponent(comp: FFComponentContainer){
        for (region in comp.component.shape.getCoveredRegions()){
            val world = comp.component.ownership?.world ?: continue
            val bvh = BVH.getBVH(world, region) ?: continue
            bvh.removeArea(comp.component)
        }
        components.remove(comp)
        comp.component.deleteSelf()
    }

    private fun fullyDeleteSelf(){
        for (comp in components){
            fullyDeleteComponent(comp)
        }
        components.clear()
        deleteSelf()
    }

    override fun writeNBT(): NbtElement {
        return toNbt()
    }

    override fun getKey(): String {
        return uuid.toString()
    }

    override fun getRegion(): String {
        return REGION_KEY
    }

}