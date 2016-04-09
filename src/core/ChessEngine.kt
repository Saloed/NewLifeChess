package core

import core.moves.MoveGenerator
import evaluator.GameScorer
import java.util.*

class ChessEngine {


    private val gameScorer: GameScorer
    var depth = DEPTH
    var qdepth = Q_DEPTH
    var scoreMargin = 1000
    var maxDeepMoves = 5
    private val quiesce = false

    constructor() {
        this.gameScorer = GameScorer.defaultScorer
    }

    constructor(depth: Int, qdepth: Int) : this() {
        this.depth = depth
        this.qdepth = qdepth
    }

    constructor(gameScorer: GameScorer) {
        this.gameScorer = gameScorer
    }

    fun getPreferredMove(bitBoard: BitBoard): String? {

        val allMoves = getScoredMoves(bitBoard)


        return selectBestMove(allMoves)
    }

    private fun getScoredMoves(bitBoard: BitBoard): MutableList<ScoredMove> {
        val rv = ArrayList<ScoredMove>()


        val moveItr = MoveGenerator(bitBoard)
        while (moveItr.hasNext()) {
            val move = moveItr.next()

            bitBoard.makeMove(move)
            val score = alphaBetaMax(Integer.MIN_VALUE, Integer.MAX_VALUE, depth, bitBoard)

            bitBoard.unmakeMove()

            rv.add(ScoredMove(move.algebraic, score))
        }

        return rv
    }

    private fun alphaBetaMaxQ(alpha: Int, beta: Int, depth: Int, bitBoard: BitBoard): Int {

        //Log.i(TAG, "QMax $depth " + Thread.currentThread().name)


        var alpha = alpha
        val qmoves = MoveGenerator(bitBoard).threateningMoves

        if (depth >= qdepth || qmoves.size == 0) {
            return gameScorer.score(bitBoard, false, alpha, beta)
        }

        for (move in qmoves) {
            bitBoard.makeMove(move)
            val score = alphaBetaMinQ(alpha, beta, depth + 1, bitBoard)
            bitBoard.unmakeMove()

            if (score >= beta) {
                return beta
            }
            alpha = Math.max(alpha, score)
        }
        return alpha
    }

    private fun alphaBetaMinQ(alpha: Int, beta: Int, depth: Int, bitBoard: BitBoard): Int {

        // Log.i(TAG, "QMin $depth " + Thread.currentThread().name)


        var beta = beta
        val qmoves = MoveGenerator(bitBoard).threateningMoves

        if (depth >= qdepth || qmoves.size == 0) {
            return -gameScorer.score(bitBoard, false, alpha, beta)
        }

        for (move in qmoves) {
            bitBoard.makeMove(move)
            val score = alphaBetaMaxQ(alpha, beta, depth + 1, bitBoard)
            bitBoard.unmakeMove()

            if (score <= alpha) {
                return alpha
            }
            beta = Math.min(beta, score)
        }
        return beta
    }

    private val TAG = "APPLICATION_DEBUG"

    private fun alphaBetaMax(alpha: Int, beta: Int, depthLeft: Int, bitBoard: BitBoard): Int {

        //  Log.i(TAG, "Max ${depth - depthLeft} " + Thread.currentThread().name)

        var alpha = alpha
        val moveItr = MoveGenerator(bitBoard)
        if (depthLeft == 0 || !moveItr.hasNext()) {
            var rv: Int
            if (quiesce) {
                if (moveItr.hasNext()) {
                    rv = alphaBetaMinQ(alpha, beta, 0, bitBoard)
                } else {
                    rv = gameScorer.score(bitBoard, false, alpha, beta)
                }
            } else {
                rv = gameScorer.score(bitBoard, false, alpha, beta)
            }
            if (rv == GameScorer.MATE_SCORE) {
                rv *= depthLeft + 1
            }
            return rv
        }

        while (moveItr.hasNext()) {
            val move = moveItr.next()

            bitBoard.makeMove(move)
            val score = alphaBetaMin(alpha, beta, depthLeft - 1, bitBoard)
            bitBoard.unmakeMove()

            if (score >= beta) {
                return beta
            }
            alpha = Math.max(alpha, score)
        }

        return alpha
    }

    private fun alphaBetaMin(alpha: Int, beta: Int, depthLeft: Int, bitBoard: BitBoard): Int {

        //  Log.i(TAG, "Min ${depth - depthLeft} " + Thread.currentThread().name)


        var beta = beta
        val moveItr = MoveGenerator(bitBoard)
        if (depthLeft == 0 || !moveItr.hasNext()) {
            var rv: Int
            if (quiesce) {
                if (moveItr.hasNext()) {
                    rv = alphaBetaMaxQ(alpha, beta, 0, bitBoard)
                } else {
                    rv = gameScorer.score(bitBoard, false, alpha, beta)
                }
            } else {
                rv = gameScorer.score(bitBoard, false, alpha, beta)
            }
            if (rv == GameScorer.MATE_SCORE) {
                rv *= depthLeft + 1
            }
            return -rv
        }

        while (moveItr.hasNext()) {
            val move = moveItr.next()

            bitBoard.makeMove(move)
            val score = alphaBetaMax(alpha, beta, depthLeft - 1, bitBoard)
            bitBoard.unmakeMove()

            if (score <= alpha) {
                return alpha
            }
            beta = Math.min(beta, score)
        }

        return beta
    }

    companion object {
        private val DEPTH = 5
        private val Q_DEPTH = 2

        /**
         * Selects all moves sharing the lowest (i.e. best) score.
         */
        private fun selectBestMoves(allMoves: MutableList<ScoredMove>, margin: Int = 0, maxMoves: Int = 5): List<ScoredMove> {
            if (allMoves.size == 0) {
                return allMoves
            }

            Collections.sort(allMoves)
            val bestScore = allMoves[0].score
            while (allMoves.size > maxMoves || allMoves[allMoves.size - 1].score > bestScore + margin) {
                allMoves.removeAt(allMoves.size - 1)
            }
            return allMoves
        }

        private fun selectBestMove(allMoves: MutableList<ScoredMove>): String? {
            val bestMoves = selectBestMoves(allMoves)
            if (bestMoves.size == 0) {
                return null
            }
            return allMoves[(Math.random() * allMoves.size).toInt()].move
        }
    }

    private class ScoredMove(var move: String?, var score: Int) : Comparable<ScoredMove> {

        override fun toString(): String {
            return move + "=" + score
        }

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + if (move == null) 0 else move!!.hashCode()
            result = prime * result + score
            return result
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj)
                return true
            if (obj == null)
                return false
            if (javaClass != obj.javaClass)
                return false
            val other = obj as ScoredMove
            if (move == null) {
                if (other.move != null)
                    return false
            } else if (move != other.move)
                return false
            if (score != other.score)
                return false
            return true
        }

        override fun compareTo(other: ScoredMove): Int {
            return score.compareTo(other.score)
        }
    }


}
