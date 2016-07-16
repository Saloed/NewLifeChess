package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.CheckDetector
import model.Piece

class PawnMoveGenerator : PieceMoveGenerator() {

    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        val playerIdx = bitBoard.player
        val shiftStrategy = BitBoard.getShiftStrategy(playerIdx)
        val myPawns = bitBoard.bitmapPawns and bitBoard.getBitmapColor(playerIdx)

        // Calculate pawns with nothing in front of them
        var movablePawns = shiftStrategy.shiftBackwardOneRank(
                shiftStrategy.shiftForwardOneRank(myPawns) and bitBoard.allPieces.inv())

        // As we know which pawns can move one, see which of them can move two.
        var doubleMovePawns = movablePawns and BitBoard.getRankMap(shiftStrategy.pawnStartRank)
        doubleMovePawns = shiftStrategy.shiftBackward(
                shiftStrategy.shiftForward(doubleMovePawns, 2) and bitBoard.allPieces.inv(), 2)

        while (movablePawns != 0L) {
            val nextPawn = java.lang.Long.lowestOneBit(movablePawns)
            movablePawns = movablePawns xor nextPawn
            val doubleMove = doubleMovePawns and nextPawn != 0L
            val safeFromCheck = (nextPawn and potentialPins == 0L) and !alreadyInCheck

            val toSquare = shiftStrategy.shiftForwardOneRank(nextPawn)
            if (safeFromCheck) {
                if (toSquare and BitBoard.FINAL_RANKS != 0L) {
                    rv.addAll(BitBoard.generatePromotions(nextPawn, toSquare, playerIdx))
                } else {
                    rv.add(BitBoard.generateMove(nextPawn, toSquare, playerIdx, Piece.PAWN))
                    if (doubleMove) {
                        rv.add(BitBoard.generateDoubleAdvanceMove(
                                nextPawn, shiftStrategy.shiftForward(nextPawn, 2), playerIdx))
                    }
                }
            } else {
                val bbMove = BitBoard.generateMove(nextPawn, toSquare, playerIdx, Piece.PAWN)
                bitBoard.makeMove(bbMove)
                if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard, !alreadyInCheck)) {
                    if (toSquare and BitBoard.FINAL_RANKS != 0L) {
                        rv.addAll(BitBoard.generatePromotions(nextPawn, toSquare, playerIdx))
                    } else {
                        rv.add(bbMove)
                    }
                }
                bitBoard.unmakeMove(bbMove)

                if (doubleMove) {
                    val pushTwo = BitBoard.generateDoubleAdvanceMove(
                            nextPawn, shiftStrategy.shiftForward(nextPawn, 2), playerIdx)
                    bitBoard.makeMove(pushTwo)
                    if (!CheckDetector.isPlayerJustMovedInCheck(bitBoard, !alreadyInCheck)) {
                        rv.add(pushTwo)
                    }
                    bitBoard.unmakeMove(pushTwo)
                }
            }
        }
    }
}
