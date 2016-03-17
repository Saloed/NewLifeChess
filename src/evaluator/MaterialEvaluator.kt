package evaluator

import core.BitBoard

class MaterialEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0

        score += 9000 * (java.lang.Long.bitCount(bitBoard.bitmapWhite and bitBoard.bitmapQueens) - java.lang.Long.bitCount(bitBoard.bitmapBlack and bitBoard.bitmapQueens))
        score += 5000 * (java.lang.Long.bitCount(bitBoard.bitmapWhite and bitBoard.bitmapRooks) - java.lang.Long.bitCount(bitBoard.bitmapBlack and bitBoard.bitmapRooks))
        score += 3000 * (java.lang.Long.bitCount(bitBoard.bitmapWhite and bitBoard.bitmapBishops) - java.lang.Long.bitCount(bitBoard.bitmapBlack and bitBoard.bitmapBishops))
        score += 3000 * (java.lang.Long.bitCount(bitBoard.bitmapWhite and bitBoard.bitmapKnights) - java.lang.Long.bitCount(bitBoard.bitmapBlack and bitBoard.bitmapKnights))
        score += 1000 * (java.lang.Long.bitCount(bitBoard.bitmapWhite and bitBoard.bitmapPawns) - java.lang.Long.bitCount(bitBoard.bitmapBlack and bitBoard.bitmapPawns))

        return score
    }
}
