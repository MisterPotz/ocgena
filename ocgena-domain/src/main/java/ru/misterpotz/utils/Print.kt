package utils

import ru.misterpotz.model.marking.Time

fun String.toIds() : Collection<String> {
    return split(" ")
}

fun mprintln(string: Any?) {
    println(string)
}

fun background(background : String): String {
    return "\u001B[48;5;${background}m"
}

fun font(color: String) : String {
    return "\u001B[38;5;${color}m"
}

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "0"
const val ANSI_RED = "1"
const val DARK_RED = "88"
const val DARK_BLUE = "17"
const val DARK_YELLOW = "136"
const val BACKGROUND = "\u001B[48;5;221m"
const val ANSI_GREEN = "2"
const val DARK_GREEN = "22"
const val ANSI_YELLOW = "3"
const val ANSI_ORANGE = "221"
const val ANSI_BLUE = "4"
const val ANSI_PURPLE = "54"
const val PURPLE_LIGHT ="147"
const val ANSI_PINK = "225"
const val ANSI_CYAN = "36"
const val ANSI_WHITE = "7"

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
