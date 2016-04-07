package api

import core.BitBoard
import core.ChessEngine
import core.moves.MoveGenerator
import model.Piece
import uci.FENParser
import java.util.*


class ChessAPI {

    private var depth: Int = 4;
    private var qdepth: Int = 2;

    private var board = BitBoard()

    private val movesHistory: Stack<HistoryMove> = Stack()


    fun startPosition() {
        board = BitBoard().initialise()
    }

    fun getFen() = FENParser.generate(board)
    fun makeMove(move: String) {
        val bitBoardMove = board.getMove(move)
        val count = board.moveCount
        val number = board.moveNumber
        val player = currentPlayer()
        movesHistory.push(HistoryMove(count, number, player, move, bitBoardMove, getFen(), board))
        board.makeMove(bitBoardMove)
    }


    fun allPossibleMoves(): Map<Long, List<BitBoard.BitBoardMove>> {
        val all = MoveGenerator(board).allRemainingMoves
        val map = HashMap<Long, MutableList<BitBoard.BitBoardMove>>()
        for (move in all) {
            if (map.containsKey(move.fromSquare)) {
                map[move.fromSquare]!!.add(move)
            } else {
                val elem = ArrayList<BitBoard.BitBoardMove>()
                elem.add(move)
                map.put(move.fromSquare, elem)
            }
        }
        return map
    }

    fun strToMove(str: String) = board.getMove(str)

    fun currentPlayer() = board.getCurrentPlayer()

    fun unmakeMove() {
        val last = movesHistory.pop()
        board = last.board
    }

    fun getHistory() = movesHistory

    fun getLastMove() = movesHistory.peek()

    fun setPosition(fen: String) {
        board = FENParser.loadPosition(fen, board)
    }

    fun getBoardSituation(): List<SquareAndPiece> {

        var position: Long
        val result: MutableList<SquareAndPiece> = ArrayList()
        for (i in 0..63) {
            position = indexToPosition(i)
            val piece = board.getPiece(position)
            if (piece != Piece.EMPTY)
                result.add(SquareAndPiece(getFile(position), getRank(position), piece,
                        board.getColor(position)))
        }
        return result
    }

    fun setEnginePowerfull(depth: Int, qdepth: Int) {
        this.depth = depth
        this.qdepth = qdepth
    }

    fun computerMove(): String {
        val engine = ChessEngine(depth, qdepth)
        val move = engine.getPreferredMove(board) ?: return ""
        makeMove(move)
        return move
    }

    //Dont use if not really need
    fun getBoard() = board

    fun getPosition(file: Int, rank: Int): Long {
        return indexToPosition((rank * 8) + file)
    }

    fun getFile(position: Long): Int {
        val index = java.lang.Long.numberOfLeadingZeros(position)
        return (index % 8)
    }

    fun getRank(position: Long): Int {
        val index = java.lang.Long.numberOfLeadingZeros(position)
        return (index / 8)
    }

    private fun indexToPosition(i: Int) = (1L shl i)

    data class SquareAndPiece(val file: Int, val rank: Int, val piece: Int, val color: Int)
    data class HistoryMove(val moveCount: Int, val moveNumber: Int, val player: Int,
                           val move: String, val moveAsBitboardMove: BitBoard.BitBoardMove,
                           val fen: String, val board: BitBoard)


}