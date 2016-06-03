import core.BitBoard
import core.CheckDetector
import core.moves.MoveGenerator
import core.moves.PawnCaptureGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uci.FENParser
import java.util.*

class BoardTest {

    @Test
    fun knightCheck() {
        var bitBoard = BitBoard()
        FENParser.loadPosition("7k/8/8/8/8/8/5n2/7K w - - 0 4", bitBoard)
        assertEquals(true, CheckDetector.isPlayerToMoveInCheck(bitBoard))

        bitBoard = FENParser.loadPosition("7k/8/n7/8/8/6n1/8/7K w - - 0 4", bitBoard)
        assertEquals(true, CheckDetector.isPlayerToMoveInCheck(bitBoard))

        bitBoard = FENParser.loadPosition("7k/8/n7/8/8/6n1/8/7K b - - 0 4", bitBoard)
        assertEquals(false, CheckDetector.isPlayerToMoveInCheck(bitBoard))

        bitBoard = FENParser.loadPosition("7k/8/n7/8/8/5n2/8/7K w - - 0 4", bitBoard)
        assertEquals(false, CheckDetector.isPlayerToMoveInCheck(bitBoard))
    }

    @Test
    fun rookCheck() {
        val bitBoard = BitBoard()
        FENParser.loadPosition("7k/8/8/8/8/8/8/r6K w - - 0 4", bitBoard)
        assertEquals(true, CheckDetector.isPlayerToMoveInCheck(bitBoard))
    }

    @Test
    fun bishopCheck() {
        var board = BitBoard()
        FENParser.loadPosition("b6k/8/8/8/8/8/8/7K w - - 0 4", board)
        assertEquals(true, CheckDetector.isPlayerToMoveInCheck(board))

        board = FENParser.loadPosition("B6k/8/8/8/8/8/8/7K w - - 0 4", board)
        assertEquals(false, CheckDetector.isPlayerToMoveInCheck(board))
    }

    @Test
    fun knightMoves() {
        var board = BitBoard()
        FENParser.loadPosition("7k/8/8/8/8/8/7P/7K w - - 0 1", board)
        val baseMoves = MoveGenerator(board).allRemainingMoves.size

        board = FENParser.loadPosition("7k/8/8/8/3N4/8/7P/7K w - - 0 1", board)
        assertEquals((baseMoves + 8).toLong(), MoveGenerator(board).allRemainingMoves.size.toLong())

        board = FENParser.loadPosition("7k/8/8/8/8/8/7P/N6K w - - 0 1", board)
        assertEquals((baseMoves + 2).toLong(), MoveGenerator(board).allRemainingMoves.size.toLong())
    }

    @Test
    fun pawnCaptures() {
        var board = BitBoard()
        FENParser.loadPosition("7k/7p/8/bp1n2P1/1PP1P3/8/8/7K w - - 0 1", board)
        val rv = ArrayList<BitBoard.BitBoardMove>()
        PawnCaptureGenerator().generateMoves(board, false, -1L, rv)
        assertEquals(4, rv.size.toLong())

        val fen = "7k/7p/8/bp1n2P1/1PP1P3/8/8/7K b - - 0 1"
        board = FENParser.loadPosition(fen, board)

        val move = board.getMove("H7H5")
        board.makeMove(move)

        rv.clear()

        PawnCaptureGenerator().generateMoves(board, false, -1L, rv)
        assertEquals(5, rv.size.toLong())

        board = board.reverse()
        //println(java.lang.Long.toBinaryString(board.bitmapKings))

        rv.clear()
        //println("Test values")
        PawnCaptureGenerator().generateMoves(board, false, -1L, rv)
        assertEquals(5, rv.size.toLong())
        assertTrue(board.isEnPassant)
        assertEquals(7, board.enPassantFile.toLong())
        assertEquals(2, board.enPassantRank.toLong())
    }
}
