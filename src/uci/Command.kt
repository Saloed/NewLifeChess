package uci

internal interface Command {

    fun execute(args: MutableList<String>)
}
