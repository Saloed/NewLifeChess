package uci

import core.BitBoard
import core.ChessEngine

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class UCIInterface private constructor() {

    private val commands = HashMap<String, Command>()
    private var terminate = false
    private var debug = false

    private val board = BitBoard()

    init {
        commands.put("uci", CommandUCI())
        commands.put("isready", CommandIsReady())
        commands.put("debug", CommandDebug())
        commands.put("setoption", CommandSetOption())
        commands.put("register", CommandRegister())
        commands.put("ucinewgame", CommandUciNewGame())
        commands.put("position", CommandPosition())
        commands.put("go", CommandGo())
        commands.put("stop", CommandStop())
        commands.put("ponderhit", CommandPonderhit())
        commands.put("quit", CommandQuit())
    }

    @Throws(IOException::class)
    private fun startInterface() {
        val input = BufferedReader(InputStreamReader(System.`in`))
        var command: String = input.readLine()
        while ((command) != null) {
            val splitCommand: MutableList<String> =
                    ((command.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toMutableList()))
            if (splitCommand.size == 0) {
                System.err.println("Empty command received from interface")
                continue
            }
            val exec = commands[splitCommand[0]]
            if (exec == null) {
                System.err.println("Unknown/unsupported command: " + command)
                continue
            }
            splitCommand.removeAt(0)
            exec.execute(splitCommand)
            if (terminate) {
                break
            }
            command = input.readLine()
        }
    }

    private fun send(s: String) {
        println(s)
    }

    private fun terminate() {
        this.terminate = true
    }


    // ------------------------------------- Commands ---------------------------

    private inner class CommandGo : Command {

        override fun execute(args: MutableList<String>) {
            val node = ChessEngine()

            val bestMove = node.getPreferredMove(board)
            send("info pv " + bestMove)
            send("bestmove " + bestMove!!.toLowerCase())

        }
    }

    private inner class CommandQuit : Command {

        override fun execute(args: MutableList<String>) {
            this@UCIInterface.terminate()
        }
    }

    private inner class CommandUciNewGame : Command {

        override fun execute(args: MutableList<String>) {
            // Not implemented
        }
    }

    private inner class CommandStop : Command {

        override fun execute(args: MutableList<String>) {
            // Not implemented
        }
    }

    private inner class CommandRegister : Command {

        override fun execute(args: MutableList<String>) {
            // Not required
        }
    }

    private inner class CommandPosition : Command {

        override fun execute(args: MutableList<String>) {
            if (args.size < 1) {
                System.err.println("Bad 'position' command")
                return
            }

            if ("startpos" == args[0]) {
                this@UCIInterface.board.initialise()
                args.removeAt(0)
            } else if ("fen" == args[0]) {
                val fen = args[1] + " " +
                        args[2] + " " +
                        args[3] + " " +
                        args[4] + " " +
                        args[5] + " " +
                        args[6]

                FENParser.loadPosition(fen, this@UCIInterface.board)
                args.removeAt(0)
                args.removeAt(0)
                args.removeAt(0)
                args.removeAt(0)
                args.removeAt(0)
                args.removeAt(0)
                args.removeAt(0)
            }
            if (args.size == 0) {
                return
            }
            if ("moves" == args[0]) {
                args.removeAt(0)
                while (args.size > 0) {
                    if ("..." != args[0]) {
                        board.makeMove(board.getMove(args[0].toUpperCase()))
                    }
                    args.removeAt(0)
                }
            }
            println("Position: " + FENParser.generate(board))
        }
    }

    private inner class CommandPonderhit : Command {

        override fun execute(args: MutableList<String>) {
            //Not implemented
        }
    }

    private inner class CommandDebug : Command {

        override fun execute(args: MutableList<String>) {
            if (args.size < 1) {
                return
            }

            if ("on" == args[0]) {
                debug = true
            }
            if ("off" == args[0]) {
                debug = false
            }
        }
    }

    private inner class CommandIsReady : Command {

        override fun execute(args: MutableList<String>) {
            this@UCIInterface.send("readyok")
        }
    }

    private inner class CommandSetOption : Command {

        override fun execute(args: MutableList<String>) {
            // Not required
        }
    }

    private inner class CommandUCI : Command {

        override fun execute(args: MutableList<String>) {
            this@UCIInterface.send("id name NewLifeEngine")
            this@UCIInterface.send("id author Saloed")
            this@UCIInterface.send("uciok")
        }
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            val uciInterface = UCIInterface()

            try {
                uciInterface.startInterface()
            } catch (x: Exception) {
                System.err.println("x")
            }

        }
    }
}
