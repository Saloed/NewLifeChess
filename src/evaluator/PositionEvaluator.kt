package evaluator

import core.BitBoard

public interface PositionEvaluator {

    fun evaluatePosition(bitBoard: BitBoard): Int
}
