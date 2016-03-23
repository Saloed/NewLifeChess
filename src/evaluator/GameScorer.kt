package evaluator

import core.BitBoard
import core.moves.MoveGenerator
import model.Piece
import java.util.*

class GameScorer {

    private val scorers = HashSet<PositionEvaluator>()

    fun addScorer(scorer: PositionEvaluator) {
        scorers.add(scorer)
    }

    /**
     * Generate a score - positive is good for the current player. Position scorers however can stick with the
     * convention of having white as positive.
     */
    fun score(bitBoard: BitBoard, quiesce: Boolean, alpha: Int, beta: Int): Int {

        //		System.out.println("score(" + alpha + ","+beta+")");
        val result = bitBoard.stalemate

        if (result === BitBoard.RES_DRAW) {
            return 0
        }

        if (result === BitBoard.RES_BLACK_WIN || result === BitBoard.RES_WHITE_WIN) {
            return MATE_SCORE
        }

        if (quiesce) {
            return quiesce(bitBoard, alpha, beta, 0)
        }

        var score = 0

        for (scorer in scorers) {
            score += scorer.evaluatePosition(bitBoard)
        }

        return score * if (bitBoard.player == Piece.WHITE) 1 else -1
    }

    private fun quiesce(bitBoard: BitBoard, alpha: Int, beta: Int, depth: Int): Int {
        var alpha = alpha
        var depth = depth
        val standPat = this.score(bitBoard, false, alpha, beta)
        //	    if(true) return standPat;

        if (standPat >= beta) {
            return beta
        }

        if (alpha < standPat) {
            alpha = standPat
        }

        if (depth >= 0) {
            return alpha
        }

        for (move in MoveGenerator(bitBoard).threateningMoves) {
            //println("Q:" + move.algebraic)
            bitBoard.makeMove(move)
            val score = -quiesce(bitBoard, -beta, -alpha, depth++)
            bitBoard.unmakeMove()

            if (score >= beta) {
                return beta
            }
            if (score > alpha) {
                alpha = score
            }
        }

        return alpha
    }

    companion object {
        val MATE_SCORE = -100000

        val defaultScorer = unweightedScorer

        private val unweightedScorer: GameScorer
            get() {
                val rv = GameScorer()

                rv.addScorer(MaterialEvaluator())
                rv.addScorer(BishopPairEvaluator())
                rv.addScorer(BishopMobilityEvaluator())
                rv.addScorer(PawnStructureEvaluator())
                rv.addScorer(KnightEvaluator())
                rv.addScorer(RookEvaluator())
                rv.addScorer(KingSafetyEvaluator())

                return rv
            }
    }
}
