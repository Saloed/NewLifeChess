package evaluator

import core.BitBoard
import model.Piece

class RookEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0
        score += scoreRooks(bitBoard, Piece.WHITE)
        score -= scoreRooks(bitBoard, Piece.BLACK)
        return score
    }

    private fun scoreRooks(bitBoard: BitBoard, color: Int): Int {
        var score = 0

        var rookMap = bitBoard.getBitmapColor(color) and bitBoard.bitmapRooks
        while (rookMap != 0L) {
            val nextRook = java.lang.Long.lowestOneBit(rookMap)
            rookMap = rookMap xor nextRook
            val file = java.lang.Long.numberOfTrailingZeros(nextRook) % 8
            if ((bitBoard.getBitmapColor(color) and bitBoard.bitmapPawns and BitBoard.getFileMap(file)) == 0L) {
                score += OPEN_FILE_SCORE
            }
        }

        rookMap = bitBoard.getBitmapColor(color) and bitBoard.bitmapRooks
        if (java.lang.Long.bitCount(rookMap) == 2) {
            val rook1 = BitBoard.toCoords(java.lang.Long.highestOneBit(rookMap))
            val rook2 = BitBoard.toCoords(java.lang.Long.lowestOneBit(rookMap))
            if (rook1[0] == rook2[0]) {
                var connMask: Long = 0
                for (i in Math.min(rook1[1], rook2[1]) + 1..Math.max(rook1[1], rook2[1]) - 1) {
                    connMask = connMask or (1L shl (i shl 3) shl rook1[0])
                }
                if ((connMask and bitBoard.allPieces) == 0L) {
                    score += CONNECTED_BONUS
                }
            } else if (rook1[1] == rook2[1]) {
                var connMask: Long = 0
                for (i in Math.min(rook1[0], rook2[0]) + 1..Math.max(rook1[0], rook2[0]) - 1) {
                    connMask = connMask or (1L shl (rook1[1] shl 3) shl i)
                }
                if (connMask and bitBoard.allPieces == 0L) {
                    score += CONNECTED_BONUS
                }
            }
        }

        return score
    }

    companion object {
        //public for test
        val OPEN_FILE_SCORE = 150
        val CONNECTED_BONUS = 150
    }
}
