package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.Bitmaps
import model.Piece


class BishopMoveGenerator : StraightMoveGenerator() {

    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        var pieces = bitBoard.bitmapColor and bitBoard.bitmapBishops
        while (pieces != 0L) {
            val nextPiece = java.lang.Long.lowestOneBit(pieces)
            pieces = pieces xor nextPiece
            val safeFromCheck = (nextPiece and potentialPins == 0L) and !alreadyInCheck

            val mapIdx = java.lang.Long.numberOfTrailingZeros(nextPiece)
            makeUpBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_UR][mapIdx], 9, alreadyInCheck, safeFromCheck, rv)
            makeUpBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_UL][mapIdx], 7, alreadyInCheck, safeFromCheck, rv)
            makeDownBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_DR][mapIdx], 7, alreadyInCheck, safeFromCheck, rv)
            makeDownBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_DL][mapIdx], 9, alreadyInCheck, safeFromCheck, rv)
        }
    }

    override val pieceType: Int
        get() = Piece.BISHOP
}