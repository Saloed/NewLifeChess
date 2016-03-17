package evaluator

import core.BitBoard
import model.Piece

class BishopPairEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0
        score += getScore(bitBoard, Piece.WHITE)
        score -= getScore(bitBoard, Piece.BLACK)
        return score
    }

    private fun getScore(bitBoard: BitBoard, color: Int): Int {
        val colorMap = bitBoard.getBitmapColor(color)
        if (java.lang.Long.bitCount(colorMap and bitBoard.bitmapBishops) >= 2) {
            return if (colorMap and bitBoard.bitmapQueens == 0L) 150 else 300
        }
        return 0
    }
}
