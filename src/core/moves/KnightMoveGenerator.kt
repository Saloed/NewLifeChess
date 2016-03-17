package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.CheckDetector
import model.Piece

class KnightMoveGenerator : PieceMoveGenerator() {

    private fun generateMoves(bitBoard: BitBoard, pieceMap: Long, alreadyInCheck: Boolean,
                              safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {
        val player = bitBoard.player
        var knightMoves = KNIGHT_MOVES[java.lang.Long.numberOfTrailingZeros(pieceMap)]
        knightMoves = knightMoves and bitBoard.bitmapColor.inv()

        while (knightMoves != 0L) {
            val nextMove = java.lang.Long.lowestOneBit(knightMoves)
            knightMoves = knightMoves xor nextMove

            val bbMove: BitBoardMove
            if (nextMove and bitBoard.getBitmapOppColor(player) != 0L) {
                bbMove = BitBoard.generateCapture(pieceMap, nextMove, player, Piece.KNIGHT, bitBoard.getPiece(nextMove))
            } else {
                bbMove = BitBoard.generateMove(pieceMap, nextMove, player.toInt(), Piece.KNIGHT.toInt())
            }

            if (safeFromCheck) {
                rv.add(bbMove)
            } else {
                bitBoard.makeMove(bbMove)
                if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard, !alreadyInCheck)) {
                    rv.add(bbMove)
                }
                bitBoard.unmakeMove()
            }
        }
    }

    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {

        var pieces = bitBoard.bitmapColor and bitBoard.bitmapKnights
        while (pieces != 0L) {
            val nextPiece = java.lang.Long.lowestOneBit(pieces)
            pieces = pieces xor nextPiece
            val safeFromCheck = (nextPiece and potentialPins == 0L) and !alreadyInCheck
            this.generateMoves(bitBoard, nextPiece, alreadyInCheck, safeFromCheck, rv)
        }
    }

    companion object {

        // Pre-generated knight moves
        val KNIGHT_MOVES = LongArray(64)

        init {
            KNIGHT_MOVES[0] = 132096L
            KNIGHT_MOVES[8] = 33816580L
            KNIGHT_MOVES[16] = 8657044482L
            KNIGHT_MOVES[24] = 2216203387392L
            KNIGHT_MOVES[32] = 567348067172352L
            KNIGHT_MOVES[40] = 145241105196122112L
            KNIGHT_MOVES[48] = 288234782788157440L
            KNIGHT_MOVES[56] = 1128098930098176L
            KNIGHT_MOVES[1] = 329728L
            KNIGHT_MOVES[9] = 84410376L
            KNIGHT_MOVES[17] = 21609056261L
            KNIGHT_MOVES[25] = 5531918402816L
            KNIGHT_MOVES[33] = 1416171111120896L
            KNIGHT_MOVES[41] = 362539804446949376L
            KNIGHT_MOVES[49] = 576469569871282176L
            KNIGHT_MOVES[57] = 2257297371824128L
            KNIGHT_MOVES[2] = 659712L
            KNIGHT_MOVES[10] = 168886289L
            KNIGHT_MOVES[18] = 43234889994L
            KNIGHT_MOVES[26] = 11068131838464L
            KNIGHT_MOVES[34] = 2833441750646784L
            KNIGHT_MOVES[42] = 725361088165576704L
            KNIGHT_MOVES[50] = 1224997833292120064L
            KNIGHT_MOVES[58] = 4796069720358912L
            KNIGHT_MOVES[3] = 1319424L
            KNIGHT_MOVES[11] = 337772578L
            KNIGHT_MOVES[19] = 86469779988L
            KNIGHT_MOVES[27] = 22136263676928L
            KNIGHT_MOVES[35] = 5666883501293568L
            KNIGHT_MOVES[43] = 1450722176331153408L
            KNIGHT_MOVES[51] = 2449995666584240128L
            KNIGHT_MOVES[59] = 9592139440717824L
            KNIGHT_MOVES[4] = 2638848L
            KNIGHT_MOVES[12] = 675545156L
            KNIGHT_MOVES[20] = 172939559976L
            KNIGHT_MOVES[28] = 44272527353856L
            KNIGHT_MOVES[36] = 11333767002587136L
            KNIGHT_MOVES[44] = 2901444352662306816L
            KNIGHT_MOVES[52] = 4899991333168480256L
            KNIGHT_MOVES[60] = 19184278881435648L
            KNIGHT_MOVES[5] = 5277696L
            KNIGHT_MOVES[13] = 1351090312L
            KNIGHT_MOVES[21] = 345879119952L
            KNIGHT_MOVES[29] = 88545054707712L
            KNIGHT_MOVES[37] = 22667534005174272L
            KNIGHT_MOVES[45] = 5802888705324613632L
            KNIGHT_MOVES[53] = -8646761407372591104L
            KNIGHT_MOVES[61] = 38368557762871296L
            KNIGHT_MOVES[6] = 10489856L
            KNIGHT_MOVES[14] = 2685403152L
            KNIGHT_MOVES[22] = 687463207072L
            KNIGHT_MOVES[30] = 175990581010432L
            KNIGHT_MOVES[38] = 45053588738670592L
            KNIGHT_MOVES[46] = -6913025356609880064L
            KNIGHT_MOVES[54] = 1152939783987658752L
            KNIGHT_MOVES[62] = 4679521487814656L
            KNIGHT_MOVES[7] = 4202496L
            KNIGHT_MOVES[15] = 1075839008L
            KNIGHT_MOVES[23] = 275414786112L
            KNIGHT_MOVES[31] = 70506185244672L
            KNIGHT_MOVES[39] = 18049583422636032L
            KNIGHT_MOVES[47] = 4620693356194824192L
            KNIGHT_MOVES[55] = 2305878468463689728L
            KNIGHT_MOVES[63] = 9077567998918656L
        }
    }
}
