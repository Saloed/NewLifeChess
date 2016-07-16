package model

object Piece {
    const val EMPTY: Byte = 0x00

    const val PAWN: Byte = 0x01
    const val KNIGHT: Byte = 0x02
    const val BISHOP: Byte = 0x03
    const val ROOK: Byte = 0x04
    const val QUEEN: Byte = 0x05
    const val KING: Byte = 0x06

    const val WHITE: Byte = 0x00
    const val BLACK: Byte = 0x08

    const val MASK_TYPE: Byte = 0x07
    const val MASK_COLOR: Byte = 0x08

    val NAMES = arrayOf("-", "Pawn", "Knight", "Bishop", "Rook", "Queen", "King")
    val COLORS = arrayOf("White", "", "", "", "", "", "", "", "Black")
}
