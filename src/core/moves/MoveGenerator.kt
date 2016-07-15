package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.Bitmaps
import core.CheckDetector
import model.Piece
import java.util.*

class MoveGenerator(private val bitBoard: BitBoard) : Iterator<BitBoardMove> {

    private lateinit var generators: List<PieceMoveGenerator>
    private val queuedMoves = ArrayList<BitBoardMove>()
    private var genIndex = 0
    private val inCheck: Boolean
    private var potentialPins: Long = 0

    init {
        this.generators = MASTER
        this.inCheck = CheckDetector.isPlayerToMoveInCheck(bitBoard)

        val enemyDiagAttackers = bitBoard.bitmapOppColor and (bitBoard.bitmapBishops or bitBoard.bitmapQueens)
        val enemyLineAttackers = bitBoard.bitmapOppColor and (bitBoard.bitmapRooks or bitBoard.bitmapQueens)
        val myKingIdx = java.lang.Long.numberOfTrailingZeros(bitBoard.bitmapColor and bitBoard.bitmapKings)

        for (aDIR_LINE in DIR_LINE) {
            if (Bitmaps.maps2[aDIR_LINE][myKingIdx] and enemyLineAttackers != 0L) {
                potentialPins = potentialPins or Bitmaps.maps2[aDIR_LINE][myKingIdx]
            }
        }

        for (aDIR_DIAG in DIR_DIAG) {
            if (Bitmaps.maps2[aDIR_DIAG][myKingIdx] and enemyDiagAttackers != 0L) {
                potentialPins = potentialPins or Bitmaps.maps2[aDIR_DIAG][myKingIdx]
            }
        }
    }

    fun setGenerators(vararg g: PieceMoveGenerator) {
        generators = Arrays.asList(*g)
    }

    override fun hasNext(): Boolean {
        if (queuedMoves.size == 0) {
            populateMoves()
        }
        return queuedMoves.size > 0
    }

    override fun next(): BitBoardMove {
        if (queuedMoves.size == 0) {
            populateMoves()
        }
        if (queuedMoves.size == 0) {
            throw NoSuchElementException()
        }

        return queuedMoves.removeAt(0)
    }


    private fun populateMoves() {
        if (genIndex >= generators.size) {
            return
        }

        if (bitBoard.isDrawnByRule) {
            return
        }

        val nextGen = generators[genIndex++]
        nextGen.generateMoves(bitBoard, inCheck, potentialPins, queuedMoves)

        if (queuedMoves.size == 0 && genIndex < generators.size) {
            populateMoves()
        }
    }

    /**
     * generate all moves - this will be useful for testing and legal move generation
     */
    val allRemainingMoves: List<BitBoardMove>
        get() {
            queuedMoves.clear()
            generators.forEach { it.generateMoves(bitBoard, inCheck, potentialPins, queuedMoves) }
            var index = 0
            //queuedMoves.forEach { it.id = index; index++ }
            return queuedMoves
        }

    val threateningMoves: List<BitBoardMove>
        get() {
            val moves = ArrayList<BitBoardMove>()

            for (generator in generators) {
                generator.generateThreatMoves(bitBoard, inCheck, potentialPins, moves)
            }

            return moves
        }

    companion object {

        private val MASTER: List<PieceMoveGenerator>

        init {
            val list = ArrayList<PieceMoveGenerator>()
            list.add(PawnCaptureGenerator())
            list.add(KnightMoveGenerator())
            list.add(BishopMoveGenerator())
            list.add(RookMoveGenerator())
            list.add(QueenMoveGenerator())
            list.add(PawnMoveGenerator())
            list.add(KingMoveGenerator())
            MASTER = Collections.unmodifiableList(list)
        }

        private val DIR_LINE = intArrayOf(Bitmaps.BM_U.toInt(), Bitmaps.BM_D.toInt(), Bitmaps.BM_L.toInt(), Bitmaps.BM_R.toInt())
        private val DIR_DIAG = intArrayOf(Bitmaps.BM_UR.toInt(), Bitmaps.BM_DR.toInt(), Bitmaps.BM_UL.toInt(), Bitmaps.BM_DL.toInt())

        fun getPossibleMoves(bitBoard: BitBoard): Map<String, MutableList<String>> {
            val moves = HashMap<String, MutableList<String>>()
            for (moveObj in MoveGenerator(bitBoard).allRemainingMoves) {
                var move = moveObj.algebraic
                if ("O-O" == move) {
                    move = if (bitBoard.player == Piece.WHITE) "E1G1" else "E8G8"
                }
                if ("O-O-O" == move) {
                    move = if (bitBoard.player == Piece.WHITE) "E1C1" else "E8C8"
                }
                val from = move.substring(0, 2)
                if (moves[from] == null) {
                    moves.put(from, ArrayList<String>())
                }
                val toList = moves[from]
                toList!!.add(move.substring(2))
            }
            return moves
        }
    }
}
