package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.CheckDetector
import model.Piece

class PawnCaptureGenerator : PieceMoveGenerator() {


    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        val player = bitBoard.player
        //println(java.lang.Long.toBinaryString(bitBoard.bitmapKings))
        var myPawns = bitBoard.getBitmapColor(player) and bitBoard.bitmapPawns
        var enemyPieces = bitBoard.getBitmapOppColor(player)
        var epLocation: Long = -1
        if (bitBoard.isEnPassant) {
            // Just treat the enpassant square as another enemy piece.
            epLocation = 1L shl (bitBoard.enPassantRank shl 3) shl bitBoard.enPassantFile
            enemyPieces = enemyPieces or epLocation
        }

        val captureRight = if (player == Piece.WHITE)
            (enemyPieces and BitBoard.getFileMap(0).inv()).ushr(9)
        else
            enemyPieces and BitBoard.getFileMap(0).inv() shl 7
        val captureLeft = if (player == Piece.WHITE)
            (enemyPieces and BitBoard.getFileMap(7).inv()).ushr(7)
        else
            enemyPieces and BitBoard.getFileMap(7).inv() shl 9

        myPawns = myPawns and (captureLeft or captureRight)

        while (myPawns != 0L) {
            val nextPiece = java.lang.Long.lowestOneBit(myPawns)
            myPawns = myPawns xor nextPiece
            val safeFromCheck = (nextPiece and potentialPins == 0L) and !alreadyInCheck

            if (nextPiece and captureLeft != 0L) {
                val captured = if (player == Piece.WHITE) nextPiece shl 7 else nextPiece.ushr(9)
                tryCaptures(bitBoard, player, nextPiece, captured, epLocation, alreadyInCheck, safeFromCheck, rv)
            }
            if (nextPiece and captureRight != 0L) {
                val captured = if (player == Piece.WHITE) nextPiece shl 9 else nextPiece.ushr(7)
                tryCaptures(bitBoard, player, nextPiece, captured, epLocation, alreadyInCheck, safeFromCheck, rv)
            }
        }
    }

    private fun tryCaptures(bitBoard: BitBoard, player: Int, nextPiece: Long,
                            captured: Long, epLocation: Long, alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {

        val bbMove: BitBoardMove
        if (captured == epLocation) {
            bbMove = BitBoard.generateEnPassantCapture(nextPiece, captured, player.toInt())
        } else {
            bbMove = BitBoard.generateCapture(
                    nextPiece, captured, player, Piece.PAWN, bitBoard.getPiece(captured))
        }

        if (safeFromCheck) {
            if (captured and BitBoard.FINAL_RANKS == 0L) {
                rv.add(bbMove)
            } else {
                rv.add(BitBoard.generateCaptureAndPromote(
                        nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.QUEEN))
                rv.add(BitBoard.generateCaptureAndPromote(
                        nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.ROOK))
                rv.add(BitBoard.generateCaptureAndPromote(
                        nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.BISHOP))
                rv.add(BitBoard.generateCaptureAndPromote(
                        nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.KNIGHT))
            }
        } else {
            bitBoard.makeMove(bbMove)
            //println(bitBoard.getBitmapColor(bitBoard.player))
            //println(java.lang.Long.toBinaryString( bitBoard.bitmapWhite))
            if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard, !alreadyInCheck)) {
                bitBoard.unmakeMove()
                if (captured and BitBoard.FINAL_RANKS == 0L) {
                    rv.add(bbMove)
                } else {
                    rv.add(BitBoard.generateCaptureAndPromote(
                            nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.QUEEN))
                    rv.add(BitBoard.generateCaptureAndPromote(
                            nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.ROOK))
                    rv.add(BitBoard.generateCaptureAndPromote(
                            nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.BISHOP))
                    rv.add(BitBoard.generateCaptureAndPromote(
                            nextPiece, captured, player.toInt(), bitBoard.getPiece(captured), Piece.KNIGHT))
                }
            } else {
                bitBoard.unmakeMove()
            }
        }
    }
}
