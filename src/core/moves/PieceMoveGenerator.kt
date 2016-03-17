package core.moves

import core.BitBoard
import core.BitBoard.BitBoardMove
import java.util.*

abstract class PieceMoveGenerator {

    /**
     * Generate moves for the supported pieces.
     */
    abstract fun generateMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>)

    /**
     * Generate threatening moves to use in quiescence searching.
     */
    fun generateThreatMoves(bitBoard: BitBoard, alreadyInCheck: Boolean, potentialPins: Long, rv: MutableList<BitBoardMove>) {
        val tempMoves = ArrayList<BitBoardMove>()
        generateMoves(bitBoard, alreadyInCheck, potentialPins, tempMoves)
        //                continue;
        //            bitBoard.makeMove(move);
        //            if (CheckDetector.isPlayerToMoveInCheck(bitBoard)) {
        //                rv.add(move);
        //            }
        //            bitBoard.unmakeMove();
        for (i in tempMoves.indices) {
            if (tempMoves[i].isCapture)
                rv.add(tempMoves[i])
        }
        // rv.addAll(tempMoves.stream().filter(Predicate<BitBoardMove> { it.isCapture() }).collect(Collectors.toList<BitBoardMove>()))
    }
}
