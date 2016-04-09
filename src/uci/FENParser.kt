package uci

import core.BitBoard
import core.EngineHelper
import model.Piece


object FENParser {
    private val FILES = "abcdefgh"

    fun generate(bitBoard: BitBoard): String {
        val fen = generatePosition(bitBoard) +
                " " + (if (bitBoard.player == Piece.WHITE) "w" else "b") +
                " " + generateCastling(bitBoard) +
                " " + generateEnPassant(bitBoard) +
                " " + bitBoard.halfMoveCount +
                " " + bitBoard.moveNumber

        return fen
    }


    private fun generateEnPassant(board: BitBoard): String {

        if (!board.isEnPassant) {
            return "-"
        }

        val fen = Character.toLowerCase(EngineHelper.FILES[board.enPassantFile]).toString() + EngineHelper.RANKS[board.enPassantRank]

        return fen
    }

    private fun generateCastling(board: BitBoard): String {
        val fen = StringBuilder()

        if (board.castlingOptions and BitBoard.CASTLE_WKS != 0) {
            fen.append("K")
        }
        if (board.castlingOptions and BitBoard.CASTLE_WQS != 0) {
            fen.append("Q")
        }
        if (board.castlingOptions and BitBoard.CASTLE_BKS != 0) {
            fen.append("k")
        }
        if (board.castlingOptions and BitBoard.CASTLE_BQS != 0) {
            fen.append("q")
        }
        if (fen.length == 0) {
            fen.append("-")
        }

        return fen.toString()
    }

    private fun generatePosition(board: BitBoard): String {
        val fen = StringBuilder()

        for (rank in 7 downTo 0) {
            var emptyCount = 0
            for (file in 0..7) {
                val pos = 1L shl (rank shl 3) shl file
                val symbol = getSymbol(((if ((board.bitmapBlack and pos) == 0L) Piece.WHITE else Piece.BLACK) or board.getPiece(pos)))
                if (symbol == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount)
                        emptyCount = 0
                    }
                    fen.append(symbol)
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount)
            }
            if (rank > 0) {
                fen.append("/")
            }
        }

        return fen.toString()
    }

    private fun getSymbol(piece: Int): String? {

        val pieceType = EngineHelper.getType(piece)

        if (pieceType == Piece.EMPTY) {
            return null
        }

        var symbol: String? = null
        if (pieceType == Piece.PAWN) {
            symbol = "p"
        } else if (pieceType == Piece.KNIGHT) {
            symbol = "n"
        } else if (pieceType == Piece.BISHOP) {
            symbol = "b"
        } else if (pieceType == Piece.ROOK) {
            symbol = "r"
        } else if (pieceType == Piece.QUEEN) {
            symbol = "q"
        } else if (pieceType == Piece.KING) {
            symbol = "k"
        }

        if (EngineHelper.getColor(piece) == Piece.BLACK) {
            return symbol
        } else {
            return symbol!!.toUpperCase()
        }
    }

    fun loadPosition(string: String, board: BitBoard): BitBoard {
        board.clear()
        val fields = string.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val ranks = fields[0].split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        for (rank in 7 downTo 0) {
            var file = 0
            for (j in 0..ranks[7 - rank].length - 1) {
                val c = ranks[7 - rank][j]
                when (c) {
                    'P' -> board.setPiece(file, rank, (Piece.PAWN or Piece.WHITE))
                    'p' -> board.setPiece(file, rank, (Piece.PAWN or Piece.BLACK))
                    'R' -> board.setPiece(file, rank, (Piece.ROOK or Piece.WHITE))
                    'r' -> board.setPiece(file, rank, (Piece.ROOK or Piece.BLACK))
                    'N' -> board.setPiece(file, rank, (Piece.KNIGHT or Piece.WHITE))
                    'n' -> board.setPiece(file, rank, (Piece.KNIGHT or Piece.BLACK))
                    'B' -> board.setPiece(file, rank, (Piece.BISHOP or Piece.WHITE))
                    'b' -> board.setPiece(file, rank, (Piece.BISHOP or Piece.BLACK))
                    'Q' -> board.setPiece(file, rank, (Piece.QUEEN or Piece.WHITE))
                    'q' -> board.setPiece(file, rank, (Piece.QUEEN or Piece.BLACK))
                    'K' -> board.setPiece(file, rank, (Piece.KING or Piece.WHITE))
                    'k' -> board.setPiece(file, rank, (Piece.KING or Piece.BLACK))
                }
                if (c >= '1' && c <= '8') {
                    file += c - '1'
                }
                file++
            }
        }

        board.player = if ("b" == fields[1]) Piece.BLACK else Piece.WHITE
        board.castlingOptions = ((if (fields[2].contains("K")) BitBoard.CASTLE_WKS else 0) or
                (if (fields[2].contains("Q")) BitBoard.CASTLE_WQS else 0) or
                (if (fields[2].contains("k")) BitBoard.CASTLE_BKS else 0) or
                (if (fields[2].contains("q")) BitBoard.CASTLE_BQS else 0))

        if (fields[3].length == 2) {
            board.enPassantFile = FILES.indexOf(fields[3][0])
        } else {
            board.enPassantFile = -1
        }
        board.moveNumber = Integer.parseInt(fields[5])

        return board
    }

}
