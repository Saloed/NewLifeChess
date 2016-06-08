package model

object Piece {
    const val EMPTY: Int = 0x00

    const val PAWN: Int = 0x01
    const val KNIGHT: Int = 0x02
    const val BISHOP: Int = 0x03
    const val ROOK: Int = 0x04
    const val QUEEN: Int = 0x05
    const val KING: Int = 0x06

    const val WHITE: Int = 0x00
    const val BLACK: Int = 0x08

    const val MASK_TYPE: Int = 0x07
    const val MASK_COLOR: Int = 0x08

    val NAMES = arrayOf("-", "Pawn", "Knight", "Bishop", "Rook", "Queen", "King")
    val COLORS = arrayOf("White", "", "", "", "", "", "", "", "Black")
}
