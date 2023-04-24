package utils

import model.Time

fun mprintln(string: Any? = null) {
    if (string != null) {
        print(string)
    }
    spprintln()
}

expect fun spprintln()

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
