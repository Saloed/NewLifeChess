package core

import core.moves.MoveGenerator
import evaluator.GameScorer
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

const val INF = 10000

class ChessEngine {


    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.privilegedThreadFactory())

    private val gameScorer: GameScorer
    var depth = DEPTH
    var qdepth = Q_DEPTH
    //var scoreMargin = 1000
    //var maxDeepMoves = 5
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

        moveItr.forEach {
            val changeBoard = bitBoard.clone()
            tasks.add(Callable {
                changeBoard.makeMove(it)
                val score: Int
                try {

                    score = alphaBeta(-INF, INF, depth, changeBoard)

                } catch(e: Exception) {
                    Logger.getLogger("Engine").log(Level.WARNING, e.toString())
                    score = 0
                }
                return@Callable ScoredMove(it.algebraic, score)

            })
        }

        executor.invokeAll(tasks).forEach { rv.add(it.get()) }
        //tasks.forEach { rv.add(it.call()) }
        return rv
    }

    private fun alphaBeta(alph: Int, beta: Int, depthLeft: Int, bitBoard: BitBoard): Int {
        var alpha = alph
        var bestscore = -INF

        if (depthLeft == 0) {
            if (quiesce) return quiesce(alpha, beta, bitBoard, qdepth)
            else return eval(bitBoard)
        }

        val moves = MoveGenerator(bitBoard).allRemainingMoves

        nodes.incrementAndGet()

        moves.forEach {
            bitBoard.makeMove(it)
            val score = -alphaBeta(-beta, -alpha, depthLeft - 1, bitBoard)
            bitBoard.unmakeMove()

            if (score >= beta)
                return score  // fail-soft beta-cutoff
            if (score > bestscore) {
                bestscore = score
                if (score > alpha)
                    alpha = score
            }
        }
        return bestscore
    }

    private fun eval(bitBoard: BitBoard) = gameScorer.score(bitBoard)

    private fun quiesce(alph: Int, beta: Int, bitBoard: BitBoard, depth: Int): Int {
        var alpha = alph
        val standPat = eval(bitBoard)

        evalCalls.incrementAndGet()

        if (depth == 0) return standPat

        if (standPat >= beta) return standPat
        if (alpha < standPat)
            alpha = standPat

        val captures = MoveGenerator(bitBoard).threateningMoves

        if (captures.isEmpty()) return standPat

        qnodes.incrementAndGet()


        captures.forEach {

            bitBoard.makeMove(it)
            val score = -quiesce(-beta, -alpha, bitBoard, depth - 1)
            bitBoard.unmakeMove()

            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }
        return alpha
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
