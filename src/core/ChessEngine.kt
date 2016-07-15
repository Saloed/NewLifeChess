package core

import core.moves.MoveGenerator
import evaluator.GameScorer
import model.Piece
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.timerTask

const val INF = 10000

class ChessEngine {

    private val executor by lazy {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.privilegedThreadFactory())
    }

    constructor(depth: Int, qdepth: Int) {
        this.maxDepth = depth
        this.qdepth = qdepth
    }

    private val gameScorer = GameScorer.defaultScorer
    var maxDepth = DEPTH
    var qdepth = Q_DEPTH

    private val quiesce = true

    val nodes = AtomicLong(0)
    val qnodes = AtomicLong(0)
    val evalCalls = AtomicLong(0)

    @Volatile var inTime: Boolean = false

    private lateinit var board: BitBoard
    private lateinit var moves: MutableList<ScoredMove>
    private lateinit var bestMove: BitBoard.BitBoardMove

    private val table = TransTable(1024 * 1024)
    private var depth = 0

    fun getPreferredMove(bitBoard: BitBoard): String? {
        getScoredMoves(bitBoard)
        return bestMove.algebraic
    }

    fun stopExecutor() = executor.shutdownNow()


    private fun getScoredMoves(bitBoard: BitBoard) {

        nodes.set(0)
        qnodes.set(0)
        evalCalls.set(0)

        inTime = true

        table.clear()

        Timer("Chess Timer").schedule(timerTask { inTime = false }, TimeUnit.SECONDS.toMillis(25))

        board = bitBoard.clone()

        moves = MoveGenerator(board).allRemainingMoves.map { ScoredMove(it, 0) }.toMutableList()

        depth = 1

        //full root search
        var value = rootSearch(-INF, INF)

        while (inTime && depth < maxDepth) {
            depth++
            value = windowSearch(value)
        }
    }

    private fun windowSearch(value: Int): Int {

        val alpha = value - 100
        val beta = value + 100

        var temp = rootSearch(alpha, beta)
        if (temp <= alpha || temp >= beta)
            temp = rootSearch(-INF, INF)
        return temp
    }

    private fun sortMoves(moves: MutableList<ScoredMove>, current: Int) {
        var high = current
        var highScore = moves[high].score

        for (i in current + 1..moves.lastIndex)
            if (moves[i].score > highScore) {
                high = i
                highScore = moves[i].score
            }

        Collections.swap(moves, high, current)
    }

    private fun rootSearch(alph: Int, bet: Int): Int {

        var value = -INF
        var alpha = alph
        var beta = bet
        var score = 0

        //moves.sort()

        for (i in moves.indices) {
            sortMoves(moves, i)

            val move = moves[i].move
            //check for check
            if (move.isCapture && move.captureType == Piece.KING) {
                alpha = INF
                bestMove = move
            }

            board.makeMove(move)

            if (value === -INF)
                score = -alphaBeta(-beta, -alpha, 0)
            else {
                score = -alphaBeta(-alpha - 1, -alpha, 0)
                if (score > alpha)
                    score = -alphaBeta(-beta, -alpha, 0)
            }
            moves[i].score = score

            if (score > value) value = score

            board.unmakeMove(move)

            if (score > alpha) {

                bestMove = move
                alpha = score

                if (score > beta) return beta
            }
        }

        return alpha
    }


    private fun alphaBeta(alph: Int, beta: Int, currentDepth: Int, bitBoard: BitBoard = board): Int {
        var alpha = alph
        var bestscore = -INF

        if (currentDepth == depth) {
            if (quiesce) return quiesce(alpha, beta, bitBoard, qdepth)
            else return eval(bitBoard)
        }

        val moves = MoveGenerator(bitBoard).allRemainingMoves

        nodes.incrementAndGet()

        moves.forEach {
            bitBoard.makeMove(it)
            val score = -alphaBeta(-beta, -alpha, currentDepth + 1, bitBoard)
            bitBoard.unmakeMove(it)

            if (score >= beta) {
                table.put(bitBoard, TTEntry(score, currentDepth))
                return score  // fail-soft beta-cutoff
            }
            if (score > bestscore) {
                bestscore = score
                if (score > alpha)
                    alpha = score
            }
        }

        table.put(bitBoard, TTEntry(bestscore, currentDepth))
        return bestscore
    }

    private fun eval(bitBoard: BitBoard) = gameScorer.score(bitBoard)

    private fun quiesce(alph: Int, beta: Int, bitBoard: BitBoard, depthLeft: Int): Int {
        var alpha = alph
        val standPat = eval(bitBoard)

        evalCalls.incrementAndGet()

        if (depthLeft == 0 || !inTime) return standPat

        if (standPat >= beta) return standPat
        if (alpha < standPat)
            alpha = standPat

        val captures = MoveGenerator(bitBoard).threateningMoves

        if (captures.isEmpty()) return standPat

        qnodes.incrementAndGet()

        captures.forEach {

            bitBoard.makeMove(it)
            val score = -quiesce(-beta, -alpha, bitBoard, depthLeft - 1)
            bitBoard.unmakeMove(it)

            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }
        return alpha
    }

    companion object {
        private val DEPTH = 6
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
            return allMoves[(Math.random() * allMoves.size).toInt()].algebraic()
        }
    }

    private class ScoredMove(var move: BitBoard.BitBoardMove, var score: Int) : Comparable<ScoredMove> {

        override fun toString() = move.algebraic + "=" + score

        fun algebraic() = move.algebraic

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + move.hashCode()
            result = prime * result + score
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (javaClass != other.javaClass) return false
            val oth = other as ScoredMove
            if (move != oth.move) return false
            if (score != oth.score) return false
            return true
        }

        override fun compareTo(other: ScoredMove) = score.compareTo(other.score)

    }


}
