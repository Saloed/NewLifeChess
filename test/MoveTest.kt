import core.BitBoard
import core.moves.MoveGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import uci.FENParser
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MoveTest {

    @Test
    fun testPosition1() {
        val board = BitBoard().initialise()
        executeBoard(board, intArrayOf(20, 400, 8902, 197281, 4865609))
    }

    @Test
    fun testPosition2() {
        val board = BitBoard()
        FENParser.loadPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", board)
        executeBoard(board, intArrayOf(48, 2039, 97862, 4085603))
        //executeBoard(board, intArrayOf(48, 2039, 97862, 4085603, 193690690));
    }

    @Test
    fun testPosition3() {
        val board = BitBoard()
        FENParser.loadPosition("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", board)

        executeBoard(board, intArrayOf(14, 191, 2812, 43238, 674624, 11030083))
        //executeBoard(board, intArrayOf(14, 191, 2812, 43238, 674624, 11030083, 178633661));
    }

    @Test
    fun testPosition4() {
        // Tests all castling moves...
        var board = BitBoard()
        FENParser.loadPosition("r3k2r/pppq1ppp/2nbbn2/3pp3/3PP3/2NBBN2/PPPQ1PPP/R3K2R w KQkq - 0 8", board)
        executeBoard(board, intArrayOf(41, 1680, 69126, 2833127))

        board = FENParser.loadPosition("r3k2r/ppp2p1p/3bbnpB/n2Np3/q2PP3/2PB1N2/PP1Q1PPP/R3K2R w KQkq - 0 11", board)
        executeBoard(board, intArrayOf(47, 2055, 93774))
    }

    @Test
    fun testPosition5() {
        val board = BitBoard()
        FENParser.loadPosition("8/PPP4k/8/8/8/8/4Kppp/8 w - - 0 1", board)
        executeBoard(board, intArrayOf(18, 290, 5044, 89363, 1745545))
    }

    @Test
    fun testPosition6() {
        val board = BitBoard()
        FENParser.loadPosition("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - - 0 1", board)
        executeBoard(board, intArrayOf(50, 279, 13310, 54703, 2538084))
    }

    @Test
    fun testPosition7() {
        val board = BitBoard()
        FENParser.loadPosition("8/p3kp2/6p1/3r1p1p/7P/8/3p2P1/3R1K2 w - - 0 1", board)
        executeBoard(board, intArrayOf(10, 218, 2886, 63771, 927197))
    }

    @Test
    fun testPosition8() {
        val board = BitBoard()
        FENParser.loadPosition("8/1k6/8/5N2/8/4n3/8/2K5 w - - 0 1", board)
        // D1 11 ;D2 156 ;D3 1636 ;D4 20534 ;D5 223507 ;D6 2594412
        executeBoard(board, intArrayOf(11, 156, 1636, 20534, 223507, 2594412))
    }

    private fun executeBoard(board: BitBoard, expect: IntArray) {
        for (x in expect.indices) {
            assertEquals(expect[x].toLong(), generateToDepth(x + 1, board).toLong())
        }
    }

    fun executeBoard(board: BitBoard, expect: LongArray, toDepth: Int = expect.lastIndex, fen: String = "", index: Int = 0) {
        for (x in 0..toDepth) {
            assertEquals("$index | $fen ", expect[x], generateToDepth(x + 1, board))
        }
    }

    private fun generateToDepth(depth: Int, bitBoard: BitBoard): Long {
        if (depth == 1) {
            return MoveGenerator(bitBoard).allRemainingMoves.size.toLong()
        }

        var count = 0L
        val moves = MoveGenerator(bitBoard).allRemainingMoves
        moves.forEach {
            val x = bitBoard.flags
            val cs = bitBoard.checksum
            bitBoard.makeMove(it)
            count += generateToDepth(depth - 1, bitBoard)
            bitBoard.unmakeMove(it)
            if (x != bitBoard.flags || cs != bitBoard.checksum) {
                throw IllegalStateException("make/unmake caused differences")
            }
        }
        return count
    }
}

fun String.input(): Scanner {
    val file = File(this)
    if (!file.isFile) throw FileNotFoundException(file.absolutePath)
    return Scanner(file, "windows-1251")
}

fun main(arg: Array<String>) {
    val fileName = "test/perftsuite.epd"
    val input = fileName.input()
    val testSuite = LinkedList<List<String>>()
    val checkValues = LinkedList<LongArray>()
    while (input.hasNextLine()) {
        val line = input.nextLine()
        if (line == "") continue
        testSuite.add(line.split(';'))
    }
    input.close()

    testSuite.forEach {
        val values = LongArray(6)
        var count = 0
        for (i in 1..it.lastIndex) {
            val str = it[i].split(' ')
            values[count] = java.lang.Long.parseLong(str[1])
            count++
        }
        checkValues.add(values)
    }
    val test = MoveTest()
    //skip first because they are too long
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val result = LinkedList<Future<String>>()
    for (i in 0..testSuite.lastIndex) {
        result.add(executor.submit(Callable<String> {
            val board = BitBoard()
            FENParser.loadPosition(testSuite[i][0], board)
            try {
                test.executeBoard(board, checkValues[i], 5, testSuite[i][0], i)
                return@Callable ("$i -> passed")
            } catch (e: AssertionError) {
                return@Callable ("$i -> unpassed " + e.toString())
            }
        }))
    }
    result.map { println(it.get()) }
    executor.shutdown()
}