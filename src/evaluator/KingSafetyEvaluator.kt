package evaluator

import core.BitBoard
import core.moves.KingMoveGenerator
import model.Piece

class KingSafetyEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0
        score += scoreSafety(bitBoard, Piece.WHITE)
        score -= scoreSafety(bitBoard, Piece.BLACK)
        return score
    }

    private fun scoreSafety(bitBoard: BitBoard, color: Int): Int {

        if ((bitBoard.getBitmapOppColor(color) or bitBoard.bitmapQueens) == 0L) {
            // Fairly crude endgame test...
            return 0
        }

        var score = 0
        val king = bitBoard.getBitmapColor(color) and bitBoard.bitmapKings
        val mapIdx = java.lang.Long.numberOfTrailingZeros(king)
        val inFront = (KingMoveGenerator.KING_MOVES[mapIdx]
        and BitBoard.getRankMap(mapIdx.ushr(3) + (if (color == Piece.WHITE) 1 else -1)) and bitBoard.getBitmapColor(color))
        //println(mapIdx)
        //println(java.lang.Long.toBinaryString(KingMoveGenerator.KING_MOVES[mapIdx]))
        //println(java.lang.Long.toBinaryString(inFront))
        score += 70 * java.lang.Long.bitCount(inFront and bitBoard.bitmapPawns)

        score += 40 * java.lang.Long.bitCount(inFront and bitBoard.bitmapPawns.inv())

        val kingPos = BitBoard.toCoords(king)
        if (kingPos[0] == 3 || kingPos[0] == 4) {
            score -= 250
        }
        return score
    }

}
