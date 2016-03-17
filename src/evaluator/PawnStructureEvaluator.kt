package evaluator

import core.BitBoard

class PawnStructureEvaluator : PositionEvaluator {

    override fun evaluatePosition(bitBoard: BitBoard): Int {

        val whitePawns = bitBoard.bitmapWhite and bitBoard.bitmapPawns
        val blackPawns = bitBoard.bitmapBlack and bitBoard.bitmapPawns

        var score = 0
        score += countIslands(blackPawns) * S_ISLAND
        score -= countIslands(whitePawns) * S_ISLAND

        score += getIsolatedCount(blackPawns) * S_ISOLATED
        score -= getIsolatedCount(whitePawns) * S_ISOLATED

        score += getDoubledScore(whitePawns, blackPawns)
        score += getAdvanceScore(whitePawns, blackPawns)

        return score
    }

    private fun getAdvanceScore(whitePawns: Long, blackPawns: Long): Int {

        var score = 0
        for (rank in 3..6) {
            score += java.lang.Long.bitCount(whitePawns and BitBoard.getRankMap(rank)) * S_ADVANCE[rank - 3]
            score -= java.lang.Long.bitCount(blackPawns and BitBoard.getRankMap(7 - rank)) * S_ADVANCE[rank - 3]
        }

        return score
    }

    private fun countIslands(pawns: Long): Int {
        var inSea = true
        var count = 0
        for (file in 0..7) {
            val pawnsOnFile = pawns and BitBoard.getFileMap(file)
            if (pawnsOnFile != 0L && inSea) {
                count++
                inSea = false
            } else if (pawnsOnFile == 0L && !inSea) {
                inSea = true
            }
        }
        return count
    }

    private fun getDoubledScore(whitePawns: Long, blackPawns: Long): Int {
        var score = 0

        for (file in 0..7) {
            val wCount = java.lang.Long.bitCount(whitePawns and BitBoard.getFileMap(file))
            val bCount = java.lang.Long.bitCount(blackPawns and BitBoard.getFileMap(file))
            score -= if (wCount > 1) (wCount - 1) * S_DOUBLED else 0
            score += if (bCount > 1) (bCount - 1) * S_DOUBLED else 0
        }
        return score
    }

    private fun getIsolatedCount(pawns: Long): Int {
        var count = 0
        var prevFile: Long = 0
        var thisFile: Long = 0
        for (file in 0..7) {
            if (file == 0) {
                thisFile = pawns and BitBoard.getFileMap(file)
            }
            val nextFile = if (file == 7) 0 else pawns and BitBoard.getFileMap(file + 1)

            if (thisFile != 0L && prevFile == 0L && nextFile == 0L) {
                count += java.lang.Long.bitCount(thisFile)
            }
            prevFile = thisFile
            thisFile = nextFile
        }
        return count
    }

    companion object {

        //public for test
        val S_ISLAND = 100
        val S_ISOLATED = 100
        val S_DOUBLED = 100

        private val S_ADVANCE = intArrayOf(20, 100, 200, 400)
    }
}
