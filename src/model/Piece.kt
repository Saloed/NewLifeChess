package model

object Piece {
    val EMPTY: Int = 0x00

    val PAWN: Int = 0x01
    val KNIGHT: Int = 0x02
    val BISHOP: Int = 0x03
    val ROOK: Int = 0x04
    val QUEEN: Int = 0x05
    val KING: Int = 0x06

    val WHITE: Int = 0x00
    val BLACK: Int = 0x08

    val MASK_TYPE: Int = 0x07
    val MASK_COLOR: Int = 0x08

    val NAMES = arrayOf("-", "Pawn", "Knight", "Bishop", "Rook", "Queen", "King")
    val COLORS = arrayOf("White", "", "", "", "", "", "", "", "Black")
}
