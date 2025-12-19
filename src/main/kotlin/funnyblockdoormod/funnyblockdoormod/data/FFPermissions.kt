package funnyblockdoormod.funnyblockdoormod.data

@JvmInline
value class FFPermissions(val bits: ULong) {
    operator fun plus(p: FFPermission): FFPermissions = FFPermissions(bits or p.mask)
    operator fun plus(other: FFPermissions): FFPermissions = FFPermissions(bits or other.bits)

    operator fun minus(p: FFPermission): FFPermissions = FFPermissions(bits and p.mask.inv())
    operator fun minus(other: FFPermissions): FFPermissions = FFPermissions(bits and other.bits.inv())

    operator fun contains(p: FFPermission): Boolean = (bits and p.mask) != 0uL
    fun has(p: FFPermission): Boolean = p in this

    fun isEmpty(): Boolean = bits == 0uL
    fun toULong(): ULong = bits

    companion object {
        val NONE: FFPermissions = FFPermissions(0uL)
        fun of(vararg perms: FFPermission): FFPermissions = FFPermissions(perms.fold(0uL) { acc, p -> acc or p.mask })
        fun fromBits(bits: ULong): FFPermissions = FFPermissions(bits)
        val ALL: FFPermissions = FFPermissions(ULong.MAX_VALUE)
    }
}