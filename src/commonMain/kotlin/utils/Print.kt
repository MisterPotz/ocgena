package utils

import model.Time

fun mprintln(string: Any? = null) {
    if (string != null) {
        print(string)
    }
    spprintln()
}

expect fun spprintln()

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_ORANGE = "\u001B[38;5;221m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_PINK = "\u001B[38;5;225m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

fun Time.print() : String {
    return "${this}t"
}

fun String.indent(times: Int): String {
    return this.prependIndent(
        (0 until times).fold("") { accum, ind ->
            accum + "\t"
        }
    )
}
fun String.indent(times: Int, prefix: String): String {
    return this.prependIndent(
        (0 until times).fold("") { accum, ind ->
            accum + "\t"
        } + prefix
    )
}


fun String.indentMargin(times: Int, margin: String = "|"): String {
    val indentBeforeMargin = (times - 1).coerceAtLeast(0)
    val indentAfterMargin = 1

    return this.prependIndent(
        (0 until indentBeforeMargin).fold("") { accum, ind ->
            accum + "\t"
        } + "$margin\t"
    )
}
