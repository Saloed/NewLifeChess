package evaluator

import core.BitBoard

interface PositionEvaluator {

    fun evaluatePosition(bitBoard: BitBoard): Int
}
