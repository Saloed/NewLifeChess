package core.moves

interface ShiftStrategy {

    fun shiftForwardOneRank(pos: Long): Long
    fun shiftForward(pos: Long, distance: Int): Long
    fun shiftBackwardOneRank(pos: Long): Long
    fun shiftBackward(pos: Long, distance: Int): Long

    val pawnStartRank: Int

    abstract class BasicStrategy : ShiftStrategy {
        override fun shiftForwardOneRank(pos: Long): Long {
            return shiftForward(pos, 1)
        }

        override fun shiftBackwardOneRank(pos: Long): Long {
            return shiftBackward(pos, 1)
        }
    }

    class WhiteStrategy : BasicStrategy() {
        override fun shiftForward(pos: Long, distance: Int): Long {
            // Move everything 1 -> 8 and clear back rank
            return pos shl 8 * distance
        }

        override fun shiftBackward(pos: Long, distance: Int): Long {
            // Move everything in direction 8 -> 1, new row is automatically empty.
            return pos.ushr(8 * distance)
        }

        override val pawnStartRank: Int
            get() = 1
    }

    class BlackStrategy : BasicStrategy() {
        override fun shiftForward(pos: Long, distance: Int): Long {
            // Move everything in direction 8 -> 1, new row is automatically empty.
            return pos.ushr(8 * distance)
        }

        override fun shiftBackward(pos: Long, distance: Int): Long {
            // Move everything 1 -> 8 and clear back rank
            return pos shl 8 * distance
        }

        override val pawnStartRank: Int
            get() = 6
    }

    companion object {
        val WHITE: ShiftStrategy = WhiteStrategy()
        val BLACK: ShiftStrategy = BlackStrategy()
    }
}
