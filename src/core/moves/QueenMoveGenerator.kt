package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.Bitmaps
import model.Piece


class QueenMoveGenerator : StraightMoveGenerator() {

    override fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        var pieces = bitBoard.bitmapColor and bitBoard.bitmapQueens
        while (pieces != 0L) {
            val nextPiece = java.lang.Long.lowestOneBit(pieces)
            pieces = pieces xor nextPiece
            val safeFromCheck = (nextPiece and potentialPins == 0L) and !alreadyInCheck

            val mapIdx = java.lang.Long.numberOfTrailingZeros(nextPiece)
            makeUpBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_U][mapIdx], 8, alreadyInCheck, safeFromCheck, rv)
            makeUpBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_R][mapIdx], 1, alreadyInCheck, safeFromCheck, rv)
            makeDownBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_L][mapIdx], 1, alreadyInCheck, safeFromCheck, rv)
            makeDownBoardMoves(bitBoard, nextPiece,
                    Bitmaps.maps2[Bitmaps.BM_D][mapIdx], 8, alreadyInCheck, safeFromCheck, rv)
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

    override val pieceType: Byte
        get() = Piece.QUEEN
}
