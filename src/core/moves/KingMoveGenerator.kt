package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.CheckDetector
import model.Piece

class KingMoveGenerator : PieceMoveGenerator() {

    private fun generateCaptureMoves(bitBoard: BitBoard, kingPos: Long, rv: MutableList<BitBoardMove>) {
        val player = bitBoard.player
        var kingMoves = KING_MOVES[java.lang.Long.numberOfTrailingZeros(kingPos)]
        kingMoves = kingMoves and bitBoard.getBitmapOppColor(player)
        while (kingMoves != 0L) {
            val nextMove = java.lang.Long.lowestOneBit(kingMoves)
            kingMoves = kingMoves xor nextMove
            val bbMove = BitBoard.generateCapture(
                    kingPos, nextMove, player, Piece.KING, bitBoard.getPiece(nextMove))
            bitBoard.makeMove(bbMove)
            if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard)) {
                rv.add(bbMove)
            }
            bitBoard.unmakeMove()
        }
    }

    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        val player = bitBoard.player

        // There can be only one...
        var king = bitBoard.getBitmapColor(player) and bitBoard.bitmapKings
        var emptyMoves = KING_MOVES[java.lang.Long.numberOfTrailingZeros(king)] and bitBoard.allPieces.inv()

        while (emptyMoves != 0L) {
            val nextMove = java.lang.Long.lowestOneBit(emptyMoves)
            emptyMoves = emptyMoves xor nextMove
            val bbMove = BitBoard.generateMove(king, nextMove, player.toInt(), Piece.KING.toInt())
            bitBoard.makeMove(bbMove)
            if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard)) {
                rv.add(bbMove)
            }
            bitBoard.unmakeMove()
        }

        val castleFlags = bitBoard.castlingOptions
        if (player == Piece.WHITE && !alreadyInCheck) {
            if (castleFlags and BitBoard.CASTLE_WKS != 0 && bitBoard.allPieces and EMPTY_WKS == 0L) {
                if (!isIntermediateCheck(bitBoard, king, king shl 1, player)) {
                    if (isCastlingPossible(bitBoard, player, BitBoard.CASTLE_WKS)) {
                        rv.add(BitBoard.generateCastling(BitBoard.CASTLE_WKS))
                    }
                }
            }
            if (castleFlags and BitBoard.CASTLE_WQS != 0 && bitBoard.allPieces and EMPTY_WQS == 0L) {
                if (!isIntermediateCheck(bitBoard, king, king.ushr(1), player)) {
                    if (isCastlingPossible(bitBoard, player, BitBoard.CASTLE_WQS)) {
                        rv.add(BitBoard.generateCastling(BitBoard.CASTLE_WQS))
                    }
                }
            }
        } else if (player == Piece.BLACK && !alreadyInCheck) {
            if (castleFlags and BitBoard.CASTLE_BKS != 0 && bitBoard.allPieces and EMPTY_BKS == 0L) {
                if (!isIntermediateCheck(bitBoard, king, king shl 1, player)) {
                    if (isCastlingPossible(bitBoard, player, BitBoard.CASTLE_BKS)) {
                        rv.add(BitBoard.generateCastling(BitBoard.CASTLE_BKS))
                    }
                }
            }
            if (castleFlags and BitBoard.CASTLE_BQS != 0 && bitBoard.allPieces and EMPTY_BQS == 0L) {
                if (!isIntermediateCheck(bitBoard, king, king.ushr(1), player)) {
                    if (isCastlingPossible(bitBoard, player, BitBoard.CASTLE_BQS)) {
                        rv.add(BitBoard.generateCastling(BitBoard.CASTLE_BQS))
                    }
                }
            }
        }

        while (king != 0L) {
            val nextPiece = java.lang.Long.lowestOneBit(king)
            king = king xor nextPiece
            this.generateCaptureMoves(bitBoard, nextPiece, rv)
        }
    }

    private fun isCastlingPossible(bitBoard: BitBoard, player: Int, castleDir: Int): Boolean {
        val bbMove = BitBoard.generateCastling(castleDir)
        bitBoard.makeMove(bbMove)
        val rv = !CheckDetector.isPlayerJustMovedInCheck(bitBoard)
        bitBoard.unmakeMove()
        return rv
    }

    private fun isIntermediateCheck(bitBoard: BitBoard, fromSquare: Long, toSquare: Long, player: Int): Boolean {
        val bbMove = BitBoard.generateMove(fromSquare, toSquare, player.toInt(), Piece.KING.toInt())
        bitBoard.makeMove(bbMove)
        val rv = CheckDetector.isPlayerJustMovedInCheck(bitBoard)
        bitBoard.unmakeMove()
        return rv
    }

    companion object {

        private val EMPTY_WKS = 3L shl 5
        private val EMPTY_WQS = 7L shl 1
        private val EMPTY_BKS = 3L shl 61
        private val EMPTY_BQS = 7L shl 57

        // Pre-generated king moves
        val KING_MOVES = LongArray(64)

        init {
            KING_MOVES[0] = 0b0000000000000000000000000000000000000000000000000000001100000010L
            KING_MOVES[1] = 0b0000000000000000000000000000000000000000000000000000011100000101L
            KING_MOVES[2] = 0b0000000000000000000000000000000000000000000000000000111000001010L
            KING_MOVES[3] = 0b0000000000000000000000000000000000000000000000000001110000010100L
            KING_MOVES[4] = 0b0000000000000000000000000000000000000000000000000011100000101000L
            KING_MOVES[5] = 0b0000000000000000000000000000000000000000000000000111000001010000L
            KING_MOVES[6] = 0b0000000000000000000000000000000000000000000000001110000010100000L
            KING_MOVES[7] = 0b0000000000000000000000000000000000000000000000001100000001000000L
            KING_MOVES[8] = 0b0000000000000000000000000000000000000000000000110000001000000011L
            KING_MOVES[9] = 0b0000000000000000000000000000000000000000000001110000010100000111L
            KING_MOVES[10] = 0b0000000000000000000000000000000000000000000011100000101000001110L
            KING_MOVES[11] = 0b0000000000000000000000000000000000000000000111000001010000011100L
            KING_MOVES[12] = 0b0000000000000000000000000000000000000000001110000010100000111000L
            KING_MOVES[13] = 0b0000000000000000000000000000000000000000011100000101000001110000L
            KING_MOVES[14] = 0b0000000000000000000000000000000000000000111000001010000011100000L
            KING_MOVES[15] = 0b0000000000000000000000000000000000000000110000000100000011000000L
            KING_MOVES[16] = 0b0000000000000000000000000000000000000011000000100000001100000000L
            KING_MOVES[17] = 0b0000000000000000000000000000000000000111000001010000011100000000L
            KING_MOVES[18] = 0b0000000000000000000000000000000000001110000010100000111000000000L
            KING_MOVES[19] = 0b0000000000000000000000000000000000011100000101000001110000000000L
            KING_MOVES[20] = 0b0000000000000000000000000000000000111000001010000011100000000000L
            KING_MOVES[21] = 0b0000000000000000000000000000000001110000010100000111000000000000L
            KING_MOVES[22] = 0b0000000000000000000000000000000011100000101000001110000000000000L
            KING_MOVES[23] = 0b0000000000000000000000000000000011000000010000001100000000000000L
            KING_MOVES[24] = 0b0000000000000000000000000000001100000010000000110000000000000000L
            KING_MOVES[25] = 0b0000000000000000000000000000011100000101000001110000000000000000L
            KING_MOVES[26] = 0b0000000000000000000000000000111000001010000011100000000000000000L
            KING_MOVES[27] = 0b0000000000000000000000000001110000010100000111000000000000000000L
            KING_MOVES[28] = 0b0000000000000000000000000011100000101000001110000000000000000000L
            KING_MOVES[29] = 0b0000000000000000000000000111000001010000011100000000000000000000L
            KING_MOVES[30] = 0b0000000000000000000000001110000010100000111000000000000000000000L
            KING_MOVES[31] = 0b0000000000000000000000001100000001000000110000000000000000000000L
            KING_MOVES[32] = 0b0000000000000000000000110000001000000011000000000000000000000000L
            KING_MOVES[33] = 0b0000000000000000000001110000010100000111000000000000000000000000L
            KING_MOVES[34] = 0b0000000000000000000011100000101000001110000000000000000000000000L
            KING_MOVES[35] = 0b0000000000000000000111000001010000011100000000000000000000000000L
            KING_MOVES[36] = 0b0000000000000000001110000010100000111000000000000000000000000000L
            KING_MOVES[37] = 0b0000000000000000011100000101000001110000000000000000000000000000L
            KING_MOVES[38] = 0b0000000000000000111000001010000011100000000000000000000000000000L
            KING_MOVES[39] = 0b0000000000000000110000000100000011000000000000000000000000000000L
            KING_MOVES[40] = 0b0000000000000011000000100000001100000000000000000000000000000000L
            KING_MOVES[41] = 0b0000000000000111000001010000011100000000000000000000000000000000L
            KING_MOVES[42] = 0b0000000000001110000010100000111000000000000000000000000000000000L
            KING_MOVES[43] = 0b0000000000011100000101000001110000000000000000000000000000000000L
            KING_MOVES[44] = 0b0000000000111000001010000011100000000000000000000000000000000000L
            KING_MOVES[45] = 0b0000000001110000010100000111000000000000000000000000000000000000L
            KING_MOVES[46] = 0b0000000011100000101000001110000000000000000000000000000000000000L
            KING_MOVES[47] = 0b0000000011000000010000001100000000000000000000000000000000000000L
            KING_MOVES[48] = 0b0000001100000010000000110000000000000000000000000000000000000000L
            KING_MOVES[49] = 0b0000011100000101000001110000000000000000000000000000000000000000L
            KING_MOVES[50] = 0b0000111000001010000011100000000000000000000000000000000000000000L
            KING_MOVES[51] = 0b0001110000010100000111000000000000000000000000000000000000000000L
            KING_MOVES[52] = 0b0011100000101000001110000000000000000000000000000000000000000000L
            KING_MOVES[53] = 0b0111000001010000011100000000000000000000000000000000000000000000L
            KING_MOVES[54] = 0b0110000010100000111000000000000000000000000000000000000000000000L
            KING_MOVES[54] = KING_MOVES[54]or (1L shl 63)
            KING_MOVES[55] = 0b0100000001000000110000000000000000000000000000000000000000000000L
            KING_MOVES[55] = KING_MOVES[55]or (1L shl 63)
            KING_MOVES[56] = 0b0000001000000011000000000000000000000000000000000000000000000000L
            KING_MOVES[57] = 0b0000010100000111000000000000000000000000000000000000000000000000L
            KING_MOVES[58] = 0b0000101000001110000000000000000000000000000000000000000000000000L
            KING_MOVES[59] = 0b0001010000011100000000000000000000000000000000000000000000000000L
            KING_MOVES[60] = 0b0010100000111000000000000000000000000000000000000000000000000000L
            KING_MOVES[61] = 0b0101000001110000000000000000000000000000000000000000000000000000L
            KING_MOVES[62] = 0b0010000011100000000000000000000000000000000000000000000000000000L
            KING_MOVES[62] = KING_MOVES[62]or (1L shl 63)
            //println(java.lang.Long.toBinaryString(KING_MOVES[62]))
            KING_MOVES[63] = 0b0100000011000000000000000000000000000000000000000000000000000000L
        }
    }
}
