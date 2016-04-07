package core

import model.Piece

object EngineHelper {

    val FILES = "ABCDEFGH"
    val RANKS = "12345678"

    private val coords = arrayOf(
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8",
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8",
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8",
            "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8",
            "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8"
    )

    fun toCoord(oneBit: Long): String {
        val trailing = java.lang.Long.numberOfTrailingZeros(oneBit)
        return coords[trailing and 0x38 shr 3 or (trailing and 0x07 shl 3)]
    }

    fun toSimpleAlgebraic(file1: Int, rank1: Int, file2: Int, rank2: Int): String {
        return coords[file1 shl 3 or rank1] + coords[file2 shl 3 or rank2]
    }

    fun getType(piece: Int): Int {
        return (piece and Piece.MASK_TYPE)
    }

    fun getColor(piece: Int): Int {
        return (piece and Piece.MASK_COLOR)
    }

    fun getPosition(square: String): Long {
        val index = coords.indexOf(square)
        if (index < 0 || index > 63)
            throw IllegalArgumentException("square not found")

        return (1L shl index)
    }

}
