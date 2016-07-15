package core

import model.Piece
import java.util.*

data class TTEntry(val value: Int, val depth: Int)

class TransTable(val capasity: Int) {
    val table = HashMap<Long, TTEntry>(capasity, 0.95f)

    fun put(board: BitBoard, entry: TTEntry) = table.put(board.zobristHash(), entry)

    fun get(board: BitBoard) = table.get(board.zobristHash())

    fun clear() = table.clear()

}

object ZHash {

    val squares = Array(2) /*color*/ { Array(7) /*piece type*/ { Array(64) /*square*/ { randLong() } } }

    val color = randLong()

    val castle = Array(16) { randLong() } //for all castle masks

    val enPassant = Array(64) { randLong() }


    //special randomizer based on magic numbers

    private var next = 1L
    private fun randLong(): Long {
        next = next * 1103515245 + 12345
        return next
    }

    fun startHash(): Long {
        var hash = 0L

        for (i in 0..8) {
            hash = hash xor squares[0][Piece.PAWN][i + 8]
            hash = hash xor squares[1][Piece.PAWN][63 - 8 - i]
        }

        hash = hash xor squares[0][Piece.ROOK][0]
        hash = hash xor squares[0][Piece.BISHOP][1]
        hash = hash xor squares[0][Piece.KNIGHT][2]
        hash = hash xor squares[0][Piece.QUEEN][3]
        hash = hash xor squares[0][Piece.KING][4]
        hash = hash xor squares[0][Piece.KNIGHT][5]
        hash = hash xor squares[0][Piece.BISHOP][6]
        hash = hash xor squares[0][Piece.ROOK][7]

        hash = hash xor squares[1][Piece.ROOK][63]
        hash = hash xor squares[1][Piece.BISHOP][63 - 1]
        hash = hash xor squares[1][Piece.KNIGHT][63 - 2]
        hash = hash xor squares[1][Piece.QUEEN][63 - 3]
        hash = hash xor squares[1][Piece.KING][63 - 4]
        hash = hash xor squares[1][Piece.KNIGHT][63 - 5]
        hash = hash xor squares[1][Piece.BISHOP][63 - 6]
        hash = hash xor squares[1][Piece.ROOK][63 - 7]

        return hash
    }

}