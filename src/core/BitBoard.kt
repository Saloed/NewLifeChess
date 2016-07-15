package core

import core.moves.MoveGenerator
import core.moves.ShiftStrategy
import model.Piece
import java.util.*


class BitBoard {

    private var bitmaps = LongArray(9)
    var player: Int = 0
    var flags: Int = 0
        private set

    var moveCount: Int = 0
    var halfMoveCount: Int = 0
        private set
    //   private lateinit var lastMove: BitBoardMove

    //TODO: hash update
    private var hash: Long = 0

    var castlingOptions: Int
        get() = (flags and CASTLE_MASK)
        set(options) {
            flags = flags and CASTLE_MASK.inv()
            flags = flags or (options and CASTLE_MASK)
            invalidateHistory()
        }

    fun clear() {
        for (i in bitmaps.indices) {
            bitmaps[i] = 0
        }
        flags = 0
        invalidateHistory()
        hash = 0
    }

    /**
     * set a piece on a square.
     */
    fun setPiece(file: Int, rank: Int, piece: Int) {
        val pos: Long = (1L shl (rank shl 3)) shl file
        bitmaps[piece and Piece.MASK_TYPE] = bitmaps[piece and Piece.MASK_TYPE] or pos
        bitmaps[piece and Piece.MASK_COLOR] = bitmaps[piece and Piece.MASK_COLOR] or pos
        invalidateHistory()
    }

    fun setNewPlayer(player: Int) {
        this.player = player
        invalidateHistory()
    }

    private fun invalidateHistory() {
        moveCount = 0
        halfMoveCount = 0
        //lastMove = BitBoardMove.nullMove
    }

    fun initialise(): BitBoard {
        bitmaps = LongArray(9)
        bitmaps[Piece.WHITE] = 0xFFFFL
        bitmaps[Piece.BLACK] = (0xFFFFL shl 48)
        bitmaps[Piece.PAWN] = (0xFFL shl 48 or (0xFFL shl 8))
        bitmaps[Piece.ROOK] = (0x81L shl 56 or 0x81L)
        bitmaps[Piece.KNIGHT] = (0x42L shl 56 or 0x42L)
        bitmaps[Piece.BISHOP] = (0x24L shl 56 or 0x24L)
        bitmaps[Piece.QUEEN] = (0x08L shl 56 or 0x08L)
        bitmaps[Piece.KING] = (0x10L shl 56 or 0x10L)
        player = Piece.WHITE
        flags = CASTLE_MASK
        moveCount = 0
        halfMoveCount = 0
        //lastMove = BitBoardMove.nullMove


        //		System.out.println(toPrettyString(bitmaps[Piece.BLACK]));
        return this
    }

    val isEnPassant: Boolean
        get() = flags and IS_EN_PASSANT != 0

    var moveNumber: Int
        get() = (moveCount / 2 + 1)
        set(moveNumber) {
            this.moveCount = ((moveNumber - 1) * 2)
            if (getCurrentPlayer() == Piece.BLACK) {
                this.moveCount++
            }
        }

    var enPassantFile: Int
        get() = (flags and EN_PASSANT_MASK).ushr(5)
        set(file) {
            flags = flags and EN_PASSANT_MASK.inv()
            if (file != -1) {
                flags = flags or IS_EN_PASSANT
                flags = flags or (file shl 5)
            }
        }

    fun zobristHash() = hash

    val enPassantRank: Int
        get() = if (getCurrentPlayer() == Piece.WHITE) 5 else 2

    fun getCurrentPlayer(): Int {
        return (player and Piece.BLACK)
    }

    override fun toString(): String {
        val buf = StringBuilder()

        buf.append("white   ").append(toPrettyString(bitmaps[MAP_WHITE])).append("\n")
        buf.append("black   ").append(toPrettyString(bitmaps[MAP_BLACK])).append("\n")

        buf.append("pawns   ").append(toPrettyString(bitmaps[MAP_PAWNS])).append("\n")
        buf.append("rooks   ").append(toPrettyString(bitmaps[MAP_ROOKS])).append("\n")
        buf.append("knights ").append(toPrettyString(bitmaps[MAP_KNIGHTS])).append("\n")
        buf.append("bishops ").append(toPrettyString(bitmaps[MAP_BISHOPS])).append("\n")
        buf.append("queens  ").append(toPrettyString(bitmaps[MAP_QUEENS])).append("\n")
        buf.append("kings   ").append(toPrettyString(bitmaps[MAP_KINGS])).append("\n")
        buf.append("flags   ").append(Integer.toBinaryString(flags and 0xFF))
        buf.append(", player = ").append(player)

        return buf.toString()
    }

    /*
    * revert colors
    * */
    fun reverse(): BitBoard {
        for (i in bitmaps.indices) {
            bitmaps[i] = reverse(bitmaps[i])
        }
        bitmaps[7] = bitmaps[Piece.WHITE]
        bitmaps[Piece.WHITE] = bitmaps[Piece.BLACK]
        bitmaps[Piece.BLACK] = bitmaps[7]
        bitmaps[7] = 0

        player = player xor Piece.BLACK
        val newFlags: Int = (flags and 0x03 shl 2 or (flags and 0x0c).ushr(2))
        flags = flags and CASTLE_MASK.inv()
        flags = flags or newFlags

        invalidateHistory()

        return this
    }

    val bitmapColor: Long
        get() = bitmaps[player]

    fun getBitmapColor(color: Int): Long {
        return if (color == Piece.WHITE) bitmapWhite else bitmapBlack
    }

    val bitmapOppColor: Long
        get() = bitmaps[player xor Piece.BLACK]

    fun getBitmapOppColor(color: Int): Long {
        return if (color == Piece.WHITE) bitmapBlack else bitmapWhite
    }

    val bitmapWhite: Long
        get() = bitmaps[MAP_WHITE]

    val bitmapBlack: Long
        get() = bitmaps[MAP_BLACK]

    val bitmapPawns: Long
        get() = bitmaps[MAP_PAWNS]

    val bitmapRooks: Long
        get() = bitmaps[MAP_ROOKS]

    val bitmapKnights: Long
        get() = bitmaps[MAP_KNIGHTS]

    val bitmapBishops: Long
        get() = bitmaps[MAP_BISHOPS]

    val bitmapQueens: Long
        get() = bitmaps[MAP_QUEENS]

    val bitmapKings: Long
        get() = bitmaps[MAP_KINGS]

    val allPieces: Long
        get() = bitmaps[MAP_BLACK] or bitmaps[MAP_WHITE]

    val allEmpty: Long
        get() = (bitmaps[MAP_BLACK] or bitmaps[MAP_WHITE]).inv()

    fun clone(): BitBoard {
        val bb = BitBoard()
        System.arraycopy(bitmaps, 0, bb.bitmaps, 0, bitmaps.size)
        bb.player = player
        bb.flags = flags
        bb.castlingOptions = castlingOptions
        bb.halfMoveCount = halfMoveCount
        bb.moveCount = moveCount
        return bb
    }

    private fun hasMatingMaterial(): Boolean {
        if (bitmaps[MAP_QUEENS] or bitmaps[MAP_ROOKS] or bitmaps[MAP_PAWNS] != 0L) {
            return true
        }
        val minorMap = bitmaps[MAP_BISHOPS] or bitmaps[MAP_KNIGHTS]
        return (java.lang.Long.bitCount(minorMap and bitmaps[MAP_BLACK]) > 1
                || java.lang.Long.bitCount(minorMap and bitmaps[MAP_WHITE]) > 1)
    }

    fun getPiece(pos: Long): Int {
        val value = (java.lang.Long.rotateLeft(bitmaps[MAP_PAWNS] and pos, MAP_PAWNS)
                or java.lang.Long.rotateLeft(bitmaps[MAP_KNIGHTS] and pos, MAP_KNIGHTS)
                or java.lang.Long.rotateLeft(bitmaps[MAP_BISHOPS] and pos, MAP_BISHOPS)
                or java.lang.Long.rotateLeft(bitmaps[MAP_ROOKS] and pos, MAP_ROOKS)
                or java.lang.Long.rotateLeft(bitmaps[MAP_QUEENS] and pos, MAP_QUEENS)
                or java.lang.Long.rotateLeft(bitmaps[MAP_KINGS] and pos, MAP_KINGS))
        if (value == 0L) {
            return Piece.EMPTY
        }
        return (java.lang.Long.numberOfTrailingZeros(value) - java.lang.Long.numberOfTrailingZeros(pos) and 0x07)
    }

    fun getColor(pos: Long): Int {
        //var piece = getPiece(pos)
        if ((bitmaps[MAP_BLACK] and pos) != 0L) {
            return Piece.BLACK
        }
        return Piece.WHITE
    }

    private fun castle(move: BitBoardMove) {
        when (move.castleDir) {
            CASTLE_WKS -> {
                bitmaps[MAP_WHITE] = bitmaps[MAP_WHITE] xor 0xF0L
                bitmaps[MAP_KINGS] = bitmaps[MAP_KINGS] xor 0x50L
                bitmaps[MAP_ROOKS] = bitmaps[MAP_ROOKS] xor 0xA0L
            }
            CASTLE_WQS -> {
                bitmaps[MAP_WHITE] = bitmaps[MAP_WHITE] xor 0x1DL
                bitmaps[MAP_KINGS] = bitmaps[MAP_KINGS] xor 0x14L
                bitmaps[MAP_ROOKS] = bitmaps[MAP_ROOKS] xor 0x09L
            }
            CASTLE_BKS -> {
                bitmaps[MAP_BLACK] = bitmaps[MAP_BLACK] xor -1152921504606846976L
                bitmaps[MAP_KINGS] = bitmaps[MAP_KINGS] xor 5764607523034234880L
                bitmaps[MAP_ROOKS] = bitmaps[MAP_ROOKS] xor -6917529027641081856L
            }
            CASTLE_BQS -> {
                bitmaps[MAP_BLACK] = bitmaps[MAP_BLACK] xor 2089670227099910144L
                bitmaps[MAP_KINGS] = bitmaps[MAP_KINGS] xor 1441151880758558720L
                bitmaps[MAP_ROOKS] = bitmaps[MAP_ROOKS] xor 648518346341351424L
            }
        }
        flags = flags and move.castleOff.inv()
    }

    fun makeMove(move: BitBoardMove) {
        //move.previousMove = this.lastMove
        //this.lastMove = move

        move.flags = this.flags
        move.halfMoveCount = halfMoveCount

        halfMoveCount++
        moveCount++

        player = player xor Piece.BLACK
        flags = flags and EN_PASSANT_MASK.inv()

        if (move.castle) {
            castle(move)
            return
        }

        if (move.isCapture) {
            halfMoveCount = 0
            bitmaps[move.captureType] = bitmaps[move.captureType] xor move.captureSquare
            bitmaps[move.colorIndex xor 0x08] = bitmaps[move.colorIndex xor 0x08] xor move.captureSquare
        } else if (move.pieceIndex == Piece.PAWN) {
            halfMoveCount = 0
        }
        bitmaps[move.pieceIndex] = bitmaps[move.pieceIndex] xor move.xorPattern
        bitmaps[move.colorIndex] = bitmaps[move.colorIndex] xor move.xorPattern
        if (move.promote) {
            bitmaps[move.pieceIndex] = bitmaps[move.pieceIndex] xor move.toSquare
            bitmaps[move.promoteTo] = bitmaps[move.promoteTo] xor move.toSquare
        }
        if (move.enpassant) {
            flags = flags and EN_PASSANT_MASK.inv()
            flags = flags or move.epFile
        } else {
            flags = flags and EN_PASSANT_MASK.inv()
        }
        flags = flags and move.castleOff.inv()

    }

    fun unmakeMove(lastMove: BitBoardMove) {
        moveCount--
        player = player xor Piece.BLACK

        if (lastMove.castle) {
            castle(lastMove)
            this.flags = lastMove.flags
            this.halfMoveCount = lastMove.halfMoveCount
            //this.lastMove = lastMove.previousMove
            return
        }

        if (lastMove.promote) {
            bitmaps[lastMove.pieceIndex] = bitmaps[lastMove.pieceIndex] xor lastMove.toSquare
            bitmaps[lastMove.promoteTo] = bitmaps[lastMove.promoteTo] xor lastMove.toSquare
        }
        bitmaps[lastMove.pieceIndex] = bitmaps[lastMove.pieceIndex] xor lastMove.xorPattern
        bitmaps[lastMove.colorIndex] = bitmaps[lastMove.colorIndex] xor lastMove.xorPattern
        if (lastMove.isCapture) {
            bitmaps[lastMove.captureType] = bitmaps[lastMove.captureType] xor lastMove.captureSquare
            bitmaps[lastMove.colorIndex xor 0x08] = bitmaps[lastMove.colorIndex xor 0x08] xor lastMove.captureSquare
        }
        this.flags = lastMove.flags
        this.halfMoveCount = lastMove.halfMoveCount
        //this.lastMove = lastMove.previousMove
    }

    val cacheId: String
        get() {
            val c = CharArray(17)
            var i = 0
            while (i < 64) {
                c[i / 4] = (c[i / 4].toInt() or (getPiece(1L shl i) shl 12 or getPiece(1L shl i + 1) shl 8 or
                        getPiece(1L shl i + 2) shl 4 or getPiece(1L shl i + 3))).toChar()
                i += 4
            }
            c[16] = (c[16].toInt() or (flags or (player shl 8))).toChar()
            return String(c)
        }

    val checksum: Long
        get() {
            var rv: Long = 0
            for (i in bitmaps.indices) {
                rv = rv xor java.lang.Long.rotateLeft(bitmaps[i], i * 8)
            }
            return rv
        }

    // Do a fast dirty test to rule out stalemates. We look for any pieces which couldn't possibly
    // be pinned and see if they have obvious moves.
    // Any pawn move means we're not stalemated...
    // Stalemate
    val stalemate: String
        get() {

            if (isDrawnByRule) {
                return RES_DRAW
            }
            val inCheck = CheckDetector.isPlayerToMoveInCheck(this)
            if (!inCheck) {
                val myKing = bitmaps[MAP_KINGS] and bitmaps[player]
                val kingIdx = java.lang.Long.numberOfTrailingZeros(myKing)
                var possiblePins = (Bitmaps.star2Map[kingIdx]
                        and getFileMap(kingIdx and 0x07).inv() and Bitmaps.BORDER.inv() and bitmaps[player])
                possiblePins = possiblePins xor bitmaps[getCurrentPlayer()]

                var freePawns = possiblePins and bitmaps[MAP_PAWNS]
                freePawns = freePawns and (if (getCurrentPlayer() == Piece.WHITE) allPieces.ushr(8) else allPieces shl 8).inv()
                if (freePawns != 0L) {
                    return RES_NO_RESULT
                }
            }

            if (!MoveGenerator(this).hasNext()) {
                if (inCheck) {
                    return if (player == Piece.BLACK) RES_WHITE_WIN else RES_BLACK_WIN
                } else {
                    return RES_DRAW
                }
            }

            return RES_NO_RESULT
        }

//    private // No point in checking until at least 8 half moves made...
//    val repeatedCount: Int
//        get() {
//            if (halfMoveCount < 8) {
//                return 0
//            }
//
//            var repeatCount = 0
//            val clone = this.clone()
//            val moves = arrayOfNulls<BitBoardMove>(halfMoveCount)
//
//            while (halfMoveCount > 1) {
//                moves[halfMoveCount - 1] = lastMove
//                unmakeMove()
//                moves[halfMoveCount - 1] = lastMove
//                unmakeMove()
//                if (this.equalPosition(clone)) {
//                    repeatCount++
//                }
//            }
//            for (i in halfMoveCount..moves.size - 1) {
//                makeMove(moves[i]!!)
//            }
//            return repeatCount + 1
//        }

    private fun equalPosition(bb: BitBoard): Boolean {
        return bb.bitmaps[0] == bitmaps[0] && bb.bitmaps[1] == bitmaps[1]
                && bb.bitmaps[2] == bitmaps[2] && bb.bitmaps[3] == bitmaps[3]
                && bb.bitmaps[4] == bitmaps[4] && bb.bitmaps[5] == bitmaps[5]
                && bb.bitmaps[6] == bitmaps[6] && bb.bitmaps[8] == bitmaps[8]
                && bb.flags == flags && bb.player == player
    }

    // Draw by 50 move rule
    val isDrawnByRule: Boolean
        get() {
            if (halfMoveCount >= 100) {
                return true
            }

            //fixme: produce bug with unmake move (previous move doesn't exist)
//            if (this.repeatedCount >= 3) {
//                return true
//            }

            if (!hasMatingMaterial()) {
                return true
            }

            return false
        }

    /**
     * Translates coords to bits
     */
    fun getMove(move: String): BitBoardMove {
        if (move == "E1G1" && getPiece(1L shl 4) == Piece.KING && player == Piece.WHITE)
            return generateCastling(CASTLE_WKS)

        if (move == "E8G8" && getPiece(1L shl 60) == Piece.KING && player == Piece.BLACK)
            return generateCastling(CASTLE_BKS)

        if (move == "E1C1" && getPiece(1L shl 4) == Piece.KING && player == Piece.WHITE) {
            return generateCastling(CASTLE_WQS)

        }
        if (move == "E8C8" && getPiece(1L shl 60) == Piece.KING && player == Piece.BLACK) {
            return generateCastling(CASTLE_BQS)
        }


        val from = coordToPosition(move.substring(0, 2))
        val to = coordToPosition(move.substring(2, 4))

        val piece = getPiece(from)
        val pieceCap = getPiece(to)
        if (piece == Piece.PAWN &&
                Math.abs(java.lang.Long.numberOfTrailingZeros(from) - java.lang.Long.numberOfTrailingZeros(to)) == 16) {
            return generateDoubleAdvanceMove(from, to, player)
        }
        if (piece == Piece.PAWN && to and FINAL_RANKS != 0L) {
            var promoTo: Int = 0
            when (move[move.length - 1]) {
                'Q' -> promoTo = Piece.QUEEN
                'R' -> promoTo = Piece.ROOK
                'N' -> promoTo = Piece.KNIGHT
                'B' -> promoTo = Piece.BISHOP
            }
            if (pieceCap != 0) {
                return generateCaptureAndPromote(from, to, player, pieceCap, promoTo)
            } else {
                return generatePromote(from, to, player, promoTo)
            }
        }

        if (piece == Piece.PAWN && Math.abs(java.lang.Long.numberOfTrailingZeros(from) - java.lang.Long.numberOfTrailingZeros(to)) != 8 && pieceCap == 0) {
            return generateEnPassantCapture(from, to, player)
        }

        if (pieceCap != 0) {
            return generateCapture(from, to, player, piece, getPiece(to))
        } else {
            return generateMove(from, to, player, piece)
        }
    }

    /**
     * Represents all information needed to make/unmake a move on a bitboard.
     */
    class BitBoardMove {

        var id: Int = -1  //help identify moves after sort (TestEngine)

        var colorIndex: Int = 0
        var pieceIndex: Int = 0

        //type of piece to capture (pawn, knight etc.)
        var captureType: Int = 0
        var promoteTo: Int = 0
        var captureSquare: Long = 0
        var toSquare: Long = 0
        var fromSquare: Long = 0
        var castleDir: Int = 0
        var epFile: Int = 0

        var flags: Int = 0

        var xorPattern: Long = 0
        var isCapture = false
        var promote = false
        var castle = false
        var enpassant = false
        var castleOff: Int = 0
        var halfMoveCount: Int = 0
        // lateinit var previousMove: BitBoardMove

        var score: Int = 0

        constructor(castleDir: Int) {
            this.castle = true
            this.castleDir = castleDir
            if (castleDir == CASTLE_WKS || castleDir == CASTLE_WQS) {
                castleOff = (CASTLE_WKS or CASTLE_WQS)
                this.fromSquare = 1L shl 4
                if (castleDir == CASTLE_WKS) {
                    this.toSquare = 1L shl 6
                } else {
                    this.toSquare = 1L shl 2
                }

            } else {
                castleOff = (CASTLE_BKS or CASTLE_BQS)
                this.fromSquare = 1L shl 60
                if (castleDir == CASTLE_BKS) {
                    this.toSquare = 1L shl 62
                } else {
                    this.toSquare = 1L shl 58
                }
            }
        }

        constructor(fromSquare: Long, toSquare: Long, colorIndex: Int, pieceIndex: Int) {
            this.fromSquare = fromSquare
            this.toSquare = toSquare
            this.colorIndex = colorIndex
            this.pieceIndex = pieceIndex
            this.xorPattern = fromSquare or toSquare

            if (xorPattern and 0x90L != 0L) {
                castleOff = castleOff or CASTLE_WKS
            }
            if (xorPattern and 0x11L != 0L) {
                castleOff = castleOff or CASTLE_WQS
            }
            if (xorPattern and (0x90L shl 56) != 0L) {
                castleOff = castleOff or CASTLE_BKS
            }
            if (xorPattern and (0x11L shl 56) != 0L) {
                castleOff = castleOff or CASTLE_BQS
            }
        }

        constructor(fromSquare: Long, toSquare: Long, colorIndex: Int, pieceIndex: Int, captureType: Int)
        : this(fromSquare, toSquare, colorIndex, pieceIndex) {
            this.captureType = captureType
            this.isCapture = true
            this.captureSquare = toSquare
        }

        override fun equals(other: Any?): Boolean {
            if (other == null) return false;
            if (other !is BitBoardMove) return false
            if (other.fromSquare != fromSquare) return false
            if (other.toSquare != toSquare) return false
            if (other.castle != castle) return false
            if (other.isCapture != isCapture) return false
            if (other.enpassant != enpassant) return false
            if (other.promote != promote) return false
            if (other.colorIndex != colorIndex) return false
            if (other.pieceIndex != pieceIndex) return false
            return true
        }

        override fun hashCode(): Int {
            var hash = fromSquare or toSquare
            hash *= pieceIndex
            hash += 19 * colorIndex
            if (castle)
                hash += 19 * 7
            if (isCapture)
                hash += 19 * 11
            if (enpassant)
                hash += 19 * 13
            if (promote)
                hash += 19 * 17
            return hash.toInt()
        }

        val algebraic: String
            get() {
                if (castle) {
                    when (castleDir) {
                        CASTLE_WKS -> return "E1G1"
                        CASTLE_BKS -> return "E8G8"
                        CASTLE_WQS -> return "E1C1"
                        CASTLE_BQS -> return "E8C8"
                    }
                }
                var move = EngineHelper.toCoord(fromSquare) + EngineHelper.toCoord(toSquare)
                if (promote) {
                    when (promoteTo) {
                        Piece.QUEEN -> move += "=Q"
                        Piece.ROOK -> move += "=R"
                        Piece.BISHOP -> move += "=B"
                        Piece.KNIGHT -> move += "=N"
                    }
                }
                return move
            }


        override fun toString(): String {
            val TAB = ", "

            val retValue: String

            retValue = ("BitBoardMove ( "
                    + super.toString() + TAB
                    + "colorIndex = " + Piece.COLORS[this.colorIndex] + TAB
                    + "pieceIndex = " + Piece.NAMES[this.pieceIndex] + TAB
                    + if (isCapture) "captureType = " + Piece.NAMES[this.captureType] + TAB else ""
                    + if (promote) "promoteTo = " + Piece.NAMES[this.promoteTo] + TAB else ""
                    + if (isCapture) "captureSquare = " + EngineHelper.toCoord(this.captureSquare) + TAB else ""
                    + "from/to = " + EngineHelper.toCoord(this.fromSquare) + EngineHelper.toCoord(this.toSquare) + TAB
                    + if (castle) "castleDir = " + this.castleDir + TAB else ""
                    //		        + "xorPattern = " + this.xorPattern + TAB
                    + "castleOff = " + this.castleOff + TAB
                    + if (enpassant) "epFile = " + this.epFile + TAB else ""
                    + " )")

            return retValue
        }

        companion object {
            val nullMove: BitBoardMove = BitBoardMove(0, 0, 0, 0)
        }
    }

    companion object {
        val RES_NO_RESULT = "*"
        val RES_WHITE_WIN = "1-0"
        val RES_BLACK_WIN = "0-1"
        val RES_DRAW = "1/2-1/2"

        val FINAL_RANKS = 255L shl 56 or 255L
        private val MAP_WHITE = Piece.WHITE
        private val MAP_BLACK = Piece.BLACK

        private val MAP_PAWNS = Piece.PAWN
        private val MAP_KNIGHTS = Piece.KNIGHT
        private val MAP_BISHOPS = Piece.BISHOP
        private val MAP_ROOKS = Piece.ROOK
        private val MAP_QUEENS = Piece.QUEEN
        private val MAP_KINGS = Piece.KING

        val CASTLE_WQS: Int = 0x01
        val CASTLE_WKS: Int = 0x02
        val CASTLE_BQS: Int = 0x04
        val CASTLE_BKS: Int = 0x08
        private val IS_EN_PASSANT = 0x10
        private val EN_PASSANT_MASK = 0xF0
        private val CASTLE_MASK: Int = 0x0F

        fun getShiftStrategy(colour: Int): ShiftStrategy {
            return if (colour == Piece.WHITE) ShiftStrategy.WHITE else ShiftStrategy.BLACK
        }

        fun getRankMap(rank: Int): Long {
            if (rank >= 0 && rank < 8) {
                return 255L shl rank * 8
            }
            return 0
        }

        fun getFileMap(file: Int): Long {
            return 0x0101010101010101L shl file
        }

        fun toMap(file: Int, rank: Int): Long {
            return 1L shl (rank shl 3) shl file
        }

        private fun reverse(bits: Long): Long {
            //println(java.lang.Long.toBinaryString(bits))
            /*val temp=*/
            //println(java.lang.Long.toBinaryString(temp))

            return ((getRankMap(0) and bits shl 56)
                    or ((getRankMap(1) and bits) shl 40)
                    or ((getRankMap(2) and bits) shl 24)
                    or ((getRankMap(3) and bits) shl 8)
                    or ((getRankMap(4) and bits).ushr(8))
                    or ((getRankMap(5) and bits).ushr(24))
                    or ((getRankMap(6) and bits).ushr(40))
                    or ((getRankMap(7) and bits).ushr(56)))
        }

        private fun toPrettyString(value: Long): String {
            val buf = StringBuilder()
            for (i in 0..63) {
                buf.insert(0, if (value and (1L shl i) == 0L) "0" else "1")
                if (i % 8 == 7) {
                    buf.insert(0, " ")
                }
            }
            buf.delete(0, 1)
            return buf.toString()
        }

        fun toBlockString(value: Long): String {
            val buf = StringBuilder()
            val buf2 = StringBuilder()
            for (i in 0..63) {
                buf.append(if (value and (1L shl i) == 0L) "0" else "1")
                if (i % 8 == 7) {
                    buf2.insert(0, buf.toString() + "\n")
                    buf.setLength(0)
                }
            }
            return buf2.toString()
        }

        /**
         * Returns the file/rank of the lowest 1 in the bitmap
         */
        fun toCoords(bitmap: Long): IntArray {
            val zeros = java.lang.Long.numberOfTrailingZeros(bitmap)
            return intArrayOf(zeros and 0x07, zeros.ushr(3))
        }

        fun generateMove(
                fromSquare: Long, toSquare: Long, colorIndex: Int, pieceIndex: Int): BitBoardMove {
            return BitBoardMove(fromSquare, toSquare, colorIndex, pieceIndex)
        }

        fun generateDoubleAdvanceMove(
                fromSquare: Long, toSquare: Long, colorIndex: Int): BitBoardMove {
            val move = BitBoardMove(fromSquare, toSquare, colorIndex, Piece.PAWN)
            move.enpassant = true
            move.epFile = (java.lang.Long.numberOfTrailingZeros(fromSquare) and 0x07 shl 5 or IS_EN_PASSANT)
            return move
        }

        fun generateCapture(
                fromSquare: Long, toSquare: Long, colorIndex: Int, pieceIndex: Int, captureType: Int): BitBoardMove {
            return BitBoardMove(fromSquare, toSquare, colorIndex, pieceIndex, captureType)
        }

        private fun generatePromote(
                fromSquare: Long, toSquare: Long, colorIndex: Int, promoteTo: Int): BitBoardMove {
            val move = BitBoardMove(fromSquare, toSquare, colorIndex, Piece.PAWN)
            move.promote = true
            move.promoteTo = promoteTo
            return move
        }

        fun generatePromotions(fromSquare: Long, toSquare: Long, colorIndex: Int): List<BitBoardMove> {
            val moves = arrayOfNulls<BitBoardMove>(4)
            moves[0] = generatePromote(fromSquare, toSquare, colorIndex, Piece.QUEEN)
            moves[1] = generatePromote(fromSquare, toSquare, colorIndex, Piece.KNIGHT)
            moves[2] = generatePromote(fromSquare, toSquare, colorIndex, Piece.ROOK)
            moves[3] = generatePromote(fromSquare, toSquare, colorIndex, Piece.BISHOP)
            return Arrays.asList<BitBoardMove>(*moves)
        }

        fun generateCaptureAndPromote(
                fromSquare: Long, toSquare: Long, colorIndex: Int, captureType: Int, promoteTo: Int): BitBoardMove {
            val move = BitBoardMove(fromSquare, toSquare, colorIndex, Piece.PAWN, captureType)
            move.promote = true
            move.promoteTo = promoteTo
            return move
        }

        fun generateEnPassantCapture(
                fromSquare: Long, toSquare: Long, colorIndex: Int): BitBoardMove {
            val move = BitBoardMove(fromSquare, toSquare, colorIndex, Piece.PAWN, Piece.PAWN)
            move.captureSquare = if (colorIndex == Piece.WHITE) toSquare.ushr(8) else toSquare shl 8
            return move
        }

        fun generateCastling(castleDir: Int): BitBoardMove {
            return BitBoardMove(castleDir)
        }

        public fun coordToPosition(coord: String): Long {
            return 1L shl (EngineHelper.FILES.indexOf(coord[0]) or (EngineHelper.RANKS.indexOf(coord[1]) shl 3))
        }

        fun compare(bb1: BitBoard, bb2: BitBoard) {
            for (i in bb1.bitmaps.indices) {
                if (bb1.bitmaps[i] != bb2.bitmaps[i]) {
                    println("Index " + i)
                    println(toPrettyString(bb1.bitmaps[i]))
                    println(toPrettyString(bb2.bitmaps[i]))
                }
            }
            if (bb1.player != bb2.player) {
                println("Flags: " + bb1.player + " " + bb2.player)
            }
        }
    }
}
