package evaluator

import core.BitBoard
import core.moves.KnightMoveGenerator
import model.Piece

class KnightEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {
        var score = 0
        score += scoreKnights(bitBoard, Piece.WHITE)
        score -= scoreKnights(bitBoard, Piece.BLACK)
        return score
    }

    private fun scoreKnights(bitBoard: BitBoard, color: Byte): Int {
        var score = 0

        val enemyPawns = bitBoard.getBitmapOppColor(color) and bitBoard.bitmapPawns
        val enemyPawnsRight = if (color == Piece.WHITE)
            (enemyPawns and BitBoard.getFileMap(0).inv()).ushr(9)
        else
            (enemyPawns and BitBoard.getFileMap(0).inv() shl 7)
        val enemyPawnsLeft = if (color == Piece.WHITE)
            (enemyPawns and BitBoard.getFileMap(7).inv()).ushr(7)
        else
            (enemyPawns and BitBoard.getFileMap(7).inv() shl 9)

        val myPawns = bitBoard.getBitmapColor(color) and bitBoard.bitmapPawns
        val myPawnsRight = if (color == Piece.WHITE)
            myPawns and BitBoard.getFileMap(0).inv() shl 7
        else
            (myPawns and BitBoard.getFileMap(0).inv()).ushr(9)
        val myPawnsLeft = if (color == Piece.WHITE)
            myPawns and BitBoard.getFileMap(7).inv() shl 9
        else
            (myPawns and BitBoard.getFileMap(7).inv()).ushr(7)

        val pawnAttackDir = if (color == Piece.WHITE) 1 else -1
        var knightMap = bitBoard.getBitmapColor(color) and bitBoard.bitmapKnights

        /*
                println(java.lang.Long.toBinaryString(enemyPawns))
                println(java.lang.Long.toBinaryString(enemyPawnsRight))
                println(java.lang.Long.toBinaryString(enemyPawnsLeft))
                println(java.lang.Long.toBinaryString(myPawns))
                println(java.lang.Long.toBinaryString(myPawnsRight))
                println(java.lang.Long.toBinaryString(myPawnsLeft))
                println(java.lang.Long.toBinaryString(knightMap))
                println("----------------------------")
        */

        while (knightMap != 0L) {
            val nextKnight = java.lang.Long.lowestOneBit(knightMap)
            knightMap = knightMap xor nextKnight

            val position = BitBoard.toCoords(nextKnight)
            var knightMoves = KnightMoveGenerator.KNIGHT_MOVES[java.lang.Long.numberOfTrailingZeros(nextKnight)]
            //println(java.lang.Long.toBinaryString(knightMoves))

            var targetCount = 0
            val onRank = if (color == Piece.WHITE) position[1] else 7 - position[1]
            score += rankScores[onRank]
            while (knightMoves != 0L) {
                val nextSq = java.lang.Long.lowestOneBit(knightMoves)
                knightMoves = knightMoves xor nextSq

                if (nextSq and enemyPawnsLeft != 0L || nextSq and enemyPawnsRight != 0L) {
                    continue
                }

                targetCount++
            }
            // A knight on the rim is dim - penalise it.
            score += targetScores[targetCount]
            //println(score)
            /**
             * Strategically, the best place for a knight is supported by a pawn on the 4th - 6th rank
             * where it cannot be driven off by an enemy pawn. Give a decent bonus to knights in this
             * position.
             */
            if (onRank >= 3 && onRank <= 5) {
                var isSupported = false
                var isAttackable = false
                if (((nextKnight and myPawnsLeft) != 0L) || ((nextKnight and myPawnsRight) != 0L)) {
                    isSupported = true
                    var scaryPawns: Long = 0
                    var r = onRank + pawnAttackDir
                    while ((r > 0) && (r < 7)) {
                        if (position[0] > 0) {
                            scaryPawns = scaryPawns or (1L shl (r shl 3) shl (position[0] - 1))
                        }
                        if (position[0] < 7) {
                            scaryPawns = scaryPawns or (1L shl (r shl 3) shl (position[0] + 1))
                        }
                        r += pawnAttackDir
                    }
                    if ((enemyPawns and scaryPawns) != 0L) {
                        isAttackable = true
                    }
                }
                if (isSupported && !isAttackable) {
                    score += SECURE_BONUS // With ranking bonus - quite high.
                }
            }
        }
        return score
    }

    companion object {
        //public just for test
        val SECURE_BONUS = 200
        val rankScores = intArrayOf(0, 0, 20, 60, 100, 100, 40, 0)
        val targetScores = intArrayOf(-500, -450, -400, -200, -200, 0, 0, 100, 150)
    }
}
