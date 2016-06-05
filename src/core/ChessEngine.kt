package core

import core.moves.MoveGenerator
import evaluator.GameScorer
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

class ChessEngine {


    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.privilegedThreadFactory())

    private val gameScorer: GameScorer
    var depth = DEPTH
    var qdepth = Q_DEPTH
    var scoreMargin = 1000
    var maxDeepMoves = 5
    private val quiesce = true

    val nodes = AtomicLong(0)
    val qnodes = AtomicLong(0)
    val evalCalls = AtomicLong(0)

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

    fun stopExecutor() {
        executor.shutdownNow()
    }

    private fun getScoredMoves(bitBoard: BitBoard): MutableList<ScoredMove> {

        nodes.set(0)
        qnodes.set(0)
        evalCalls.set(0)

        val rv = ArrayList<ScoredMove>()
        val moveItr = MoveGenerator(bitBoard)
        val tasks = LinkedList<Callable<ScoredMove>>()

        while (moveItr.hasNext()) {
            val move = moveItr.next()
            val changeBoard = bitBoard.clone()
            tasks.add(Callable {
                changeBoard.makeMove(move)
                val score: Int
                try {
                    score = alphaBetaMax(Integer.MIN_VALUE, Integer.MAX_VALUE, depth, changeBoard)
                } catch(e: Exception) {
                    Logger.getLogger("Engine").log(Level.WARNING, e.toString())
                    score = 0
                }
                changeBoard.unmakeMove()
                return@Callable ScoredMove(move.algebraic, score)

            })
        }

        executor.invokeAll(tasks).forEach { rv.add(it.get()) }
        //tasks.forEach { rv.add(it.call()) }
        return rv
    }

    private fun alphaBetaMaxQ(alpha: Int, beta: Int, depth: Int, bitBoard: BitBoard): Int {

        var alpha = alpha

        if (depth >= qdepth) {
            evalCalls.incrementAndGet()
            return gameScorer.score(bitBoard)
        }



        val qmoves = MoveGenerator(bitBoard).threateningMoves

        if (qmoves.isEmpty()) {
            evalCalls.incrementAndGet()
            return gameScorer.score(bitBoard)
        }

        qnodes.incrementAndGet()



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

        var beta = beta
        if (depth >= qdepth) {
            evalCalls.incrementAndGet()
            return -gameScorer.score(bitBoard)
        }

        val qmoves = MoveGenerator(bitBoard).threateningMoves

        if (qmoves.isEmpty()) {
            evalCalls.incrementAndGet()
            return -gameScorer.score(bitBoard)
        }
        qnodes.incrementAndGet()


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

    private fun alphaBetaMax(alpha: Int, beta: Int, depthLeft: Int, bitBoard: BitBoard): Int {

        var alpha = alpha
        val moveItr = MoveGenerator(bitBoard)
        if (depthLeft == 0 || !moveItr.hasNext()) {
            var rv: Int
            if (quiesce) {
                if (moveItr.hasNext()) {
                    rv = alphaBetaMaxQ(alpha, beta, 0, bitBoard)
                } else {
                    evalCalls.incrementAndGet()
                    rv = gameScorer.score(bitBoard)
                }
            } else {
                evalCalls.incrementAndGet()
                rv = gameScorer.score(bitBoard)
            }
            if (rv == GameScorer.MATE_SCORE) {
                rv *= depthLeft + 1
            }
            return rv
        }


        nodes.incrementAndGet()

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

        var beta = beta
        val moveItr = MoveGenerator(bitBoard)
        if (depthLeft == 0 || !moveItr.hasNext()) {
            var rv: Int
            if (quiesce) {
                if (moveItr.hasNext()) {
                    rv = alphaBetaMinQ(alpha, beta, 0, bitBoard)
                } else {
                    evalCalls.incrementAndGet()
                    rv = gameScorer.score(bitBoard)
                }
            } else {
                evalCalls.incrementAndGet()
                rv = gameScorer.score(bitBoard)
            }
            if (rv == GameScorer.MATE_SCORE) {
                rv *= depthLeft + 1
            }
            return -rv
        }

        nodes.incrementAndGet()


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

        override fun equals(other: Any?): Boolean {
            if (this === other)
                return true
            if (other == null)
                return false
            if (javaClass != other.javaClass)
                return false
            val oth = other as ScoredMove
            if (move == null) {
                if (oth.move != null)
                    return false
            } else if (move != oth.move)
                return false
            if (score != oth.score)
                return false
            return true
        }

        override fun compareTo(other: ScoredMove): Int {
            return score.compareTo(other.score)
        }
    }


}
