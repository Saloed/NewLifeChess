package core

import core.moves.KingMoveGenerator
import core.moves.KnightMoveGenerator
import model.Piece

object CheckDetector {
    private val DIR_U = intArrayOf(0, 1)
    private val DIR_D = intArrayOf(0, -1)
    private val DIR_R = intArrayOf(1, 0)
    private val DIR_L = intArrayOf(-1, 0)
    private val DIR_UR = intArrayOf(1, 1)
    private val DIR_DR = intArrayOf(1, -1)
    private val DIR_DL = intArrayOf(-1, -1)
    private val DIR_UL = intArrayOf(-1, 1)

    /**
     * Tests whether the player who just moved has left themselves in check - i.e. it was an illegal move.
     * return True if the player moved into check.
     */
    fun isPlayerJustMovedInCheck(bitBoard: BitBoard): Boolean {
        val color = if (bitBoard.player == Piece.WHITE) Piece.BLACK else Piece.WHITE
        return inCheck(bitBoard, color, false)
    }

    fun isPlayerJustMovedInCheck(bitBoard: BitBoard, pinCheckOnly: Boolean): Boolean {
        val color = if (bitBoard.player == Piece.WHITE) Piece.BLACK else Piece.WHITE
        //println(java.lang.Integer.toHexString(color))
        return inCheck(bitBoard, color, pinCheckOnly)
    }

    fun isPlayerToMoveInCheck(bitBoard: BitBoard): Boolean {
        return inCheck(bitBoard, bitBoard.player, false)
    }

    /**
     * The pin check flag can be used for tests when a piece other than the king moved,
     * possibly exposing the king to check. As this sort of check could only be by a
     * bishop, rook or queen it is not necessary to check for checks by enemy pawns,
     * knights or king.
     */
    private fun inCheck(bitBoard: BitBoard, color: Int, pinCheckOnly: Boolean): Boolean {
        val kingMap = bitBoard.getBitmapColor(color) and bitBoard.bitmapKings
        /*println("color " + bitBoard.getBitmapColor(color))
        println("kingmap " + bitBoard.bitmapKings)
        println("Bcolor " + java.lang.Long.toBinaryString(bitBoard.getBitmapColor(color)))
        println("Bkingmap " + java.lang.Long.toBinaryString(bitBoard.bitmapKings))
        println(kingMap)*/
        val kingIdx = java.lang.Long.numberOfTrailingZeros(kingMap)
        val kingPos = BitBoard.toCoords(kingMap)

        if (!pinCheckOnly) {
            val enemyPawns = bitBoard.getBitmapOppColor(color) and bitBoard.bitmapPawns

            //mb bug here (1,8 rank)
            val checkPawns = (if (color == Piece.WHITE)
                kingMap and BitBoard.getFileMap(7).inv() shl 9 or (kingMap and BitBoard.getFileMap(0).inv() shl 7)
            else
                (kingMap and BitBoard.getFileMap(0).inv()).ushr(9) or (kingMap and BitBoard.getFileMap(7).inv()).ushr(7))

            if (enemyPawns and checkPawns != 0L) {
                return true
            }

            val kingMoves = KingMoveGenerator.KING_MOVES[kingIdx]
            if (kingMoves and bitBoard.bitmapKings and bitBoard.getBitmapOppColor(color) != 0L) {
                return true
            }

            val knightMoves = KnightMoveGenerator.KNIGHT_MOVES[kingIdx]
            if (knightMoves and bitBoard.bitmapKnights and bitBoard.getBitmapOppColor(color) != 0L) {
                return true
            }
        }

        val allEnemies = bitBoard.getBitmapOppColor(color)
        val lineAttackers = allEnemies and (bitBoard.bitmapRooks or bitBoard.bitmapQueens)
        if (lineAttackers and Bitmaps.cross2Map[kingIdx] != 0L) {

            if (Bitmaps.maps2[Bitmaps.BM_U][kingIdx] and lineAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_U, color, lineAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_D][kingIdx] and lineAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_D, color, lineAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_L][kingIdx] and lineAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_L, color, lineAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_R][kingIdx] and lineAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_R, color, lineAttackers)) {
                    return true
                }
            }
        }

        val diagAttackers = allEnemies and (bitBoard.bitmapBishops or bitBoard.bitmapQueens)
        if (diagAttackers and Bitmaps.diag2Map[kingIdx] != 0L) {
            if (Bitmaps.maps2[Bitmaps.BM_UR][kingIdx] and diagAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_UR, color, diagAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_UL][kingIdx] and diagAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_UL, color, diagAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_DR][kingIdx] and diagAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_DR, color, diagAttackers)) {
                    return true
                }
            }
            if (Bitmaps.maps2[Bitmaps.BM_DL][kingIdx] and diagAttackers != 0L) {
                if (detectLineAttackingPiece(bitBoard, kingPos, DIR_DL, color, diagAttackers)) {
                    return true
                }
            }
        }

        return false
    }

    private fun detectLineAttackingPiece(bitBoard: BitBoard, kingPos: IntArray, dir: IntArray, color: Int, attackers: Long)
            : Boolean {
        var nFile = kingPos[0] + dir[0]
        var nRank = kingPos[1] + dir[1]
        while (nFile or nRank and 0x07.inv() == 0) {
            if (bitBoard.getBitmapColor(color) and (1L shl (nRank shl 3) shl nFile) != 0L) {
                // There is a piece of my color in the way.
                return false
            }

            if (bitBoard.getBitmapOppColor(color) and (1L shl (nRank shl 3) shl nFile) != 0L) {
                // There is a piece of opponents color here.
                return attackers and (1L shl (nRank shl 3) shl nFile) != 0L
            }

            nFile += dir[0]
            nRank += dir[1]
        }
        return false
    }
}
