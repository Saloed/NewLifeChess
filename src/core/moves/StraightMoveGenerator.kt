package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import core.CheckDetector

abstract class StraightMoveGenerator : PieceMoveGenerator() {

    private val shiftUp = ShiftUpStrategy()
    private val shiftDown = ShiftDownStrategy()

    protected abstract val pieceType: Int

    private fun makeBoardThreats(bitBoard: BitBoard, source: Long, destinations: Long, distance: Int,
                                 alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>, ss: ShiftStrategy) {

        val player = bitBoard.player
        var shift = distance
        var isCapture = false

        while (!isCapture && ss.shift1(destinations, shift) and source != 0L) {
            val moveTo = ss.shift2(source, shift)
            shift += distance

            if (moveTo and bitBoard.getBitmapColor(player) != 0L) {
                return
            }

            isCapture = moveTo and bitBoard.getBitmapOppColor(player) != 0L
            val bbMove: BitBoardMove
            if (isCapture) {
                // This is a capturing move.
                bbMove = BitBoard.generateCapture(
                        source, moveTo, player, pieceType, bitBoard.getPiece(moveTo))
            } else {
                bbMove = BitBoard.generateMove(source, moveTo, player.toInt(), pieceType.toInt())
            }

            bitBoard.makeMove(bbMove)
            if (safeFromCheck || !CheckDetector.isPlayerJustMovedInCheck(bitBoard)) {
                //				if(isCapture || CheckDetector.isPlayerToMoveInCheck(bitBoard)) {
                if (isCapture) {
                    rv.add(bbMove)
                }
            }
            bitBoard.unmakeMove()
        }
    }

    private fun makeBoardMoves(bitBoard: BitBoard, source: Long, destinations: Long, distance: Int,
                               alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>, ss: ShiftStrategy) {

        val player = bitBoard.player
        var shift = distance
        var isCapture = false

        while (!isCapture && ss.shift1(destinations, shift) and source != 0L) {
            val moveTo = ss.shift2(source, shift)
            if (moveTo and bitBoard.getBitmapColor(player) != 0L) {
                return
            }

            isCapture = moveTo and bitBoard.getBitmapOppColor(player) != 0L
            val bbMove: BitBoardMove
            if (isCapture) {
                // This is a capturing move.
                bbMove = BitBoard.generateCapture(
                        source, moveTo, player, pieceType, bitBoard.getPiece(moveTo))
            } else {
                bbMove = BitBoard.generateMove(source, moveTo, player.toInt(), pieceType.toInt())
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

            shift += distance
        }
    }

    internal fun makeUpBoardMoves(bitBoard: BitBoard, source: Long,
                                  destinations: Long, distance: Int, alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {
        this.makeBoardMoves(bitBoard, source, destinations, distance, alreadyInCheck, safeFromCheck, rv, shiftUp)
    }

    internal fun makeDownBoardMoves(bitBoard: BitBoard, source: Long,
                                    destinations: Long, distance: Int, alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {
        this.makeBoardMoves(bitBoard, source, destinations, distance, alreadyInCheck, safeFromCheck, rv, shiftDown)
    }

    protected fun makeUpBoardThreats(bitBoard: BitBoard, source: Long,
                                     destinations: Long, distance: Int, alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {
        this.makeBoardThreats(bitBoard, source, destinations, distance, alreadyInCheck, safeFromCheck, rv, shiftUp)
    }

    protected fun makeDownBoardThreats(bitBoard: BitBoard, source: Long,
                                       destinations: Long, distance: Int, alreadyInCheck: Boolean, safeFromCheck: Boolean, rv: MutableList<BitBoardMove>) {
        this.makeBoardThreats(bitBoard, source, destinations, distance, alreadyInCheck, safeFromCheck, rv, shiftDown)
    }

    private interface ShiftStrategy {
        fun shift1(`val`: Long, dist: Int): Long

        fun shift2(`val`: Long, dist: Int): Long
    }

    private class ShiftUpStrategy : ShiftStrategy {
        override fun shift1(`val`: Long, dist: Int): Long {
            return `val`.ushr(dist)

        }

        override fun shift2(`val`: Long, dist: Int): Long {
            return `val` shl dist
        }
    }

    private class ShiftDownStrategy : ShiftStrategy {
        override fun shift1(`val`: Long, dist: Int): Long {
            return `val` shl dist

        }

        override fun shift2(`val`: Long, dist: Int): Long {
            return `val`.ushr(dist)
        }
    }
}
