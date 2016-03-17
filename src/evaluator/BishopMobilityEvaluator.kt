package evaluator

import core.BitBoard
import core.Bitmaps
import model.Piece

class BishopMobilityEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0
        score += getScore(bitBoard, Piece.WHITE)
        score -= getScore(bitBoard, Piece.BLACK)
        return score
    }

    private fun getScore(bitBoard: BitBoard, color: Int): Int {
        var bishopMap = bitBoard.getBitmapColor(color) and bitBoard.bitmapBishops
        var score = 0

        while (bishopMap != 0L) {
            val nextBishop = java.lang.Long.lowestOneBit(bishopMap)
            bishopMap = bishopMap xor nextBishop
            val mapIdx = java.lang.Long.numberOfTrailingZeros(nextBishop)
            val dirs = Bitmaps.diag2Map[mapIdx] and BitBoard.getRankMap(mapIdx.ushr(3) + if (color == Piece.WHITE) 1 else -1)
            val freeAdvance = dirs and bitBoard.getBitmapColor(color).inv()
            score += 75 * java.lang.Long.bitCount(freeAdvance)
        }

        return score
    }
}
