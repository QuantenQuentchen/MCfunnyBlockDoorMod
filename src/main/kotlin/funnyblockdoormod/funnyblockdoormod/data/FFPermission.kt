package funnyblockdoormod.funnyblockdoormod.data

enum class FFPermission {
    ADMIN,
    BUILD,
    USE,
    BREAK,
    TELEPORT,
    OCCUPY;

    val mask: ULong get() = 1uL shl ordinal

}