package utils.html

fun color(
    lines: List<String>,
    fontColor: String? = null,
    backgroundColor: String? = null
): List<String> {
    return lines.map {
        color(it, fontColor, backgroundColor)
    }
}

fun indentLines(
    indentation: Int,
    lines: List<String>,
    marginSymbol: String? = null,
): List<String> {
    val marginSymbol = marginSymbol ?: ""
    val indentBeforeSymbol = (indentation - 1).coerceAtLeast(0)
    val indentAfterSymbol = 1
    return lines.map {
        """<span style="padding-left: ${indentBeforeSymbol}em;">$marginSymbol<span style="padding-left: ${indentAfterSymbol}em;">$it</span>"""
    }
}

fun indentLines(
    indentation: Int,
    item: String,
    marginSymbol: String? = null,
): List<String> {
    return indentLines(indentation, listOf(item), marginSymbol)
}

fun indentLinesRoot(
    indentation: Int,
    lines: List<String>,
    marginSymbol: String? = null,
): List<String> {
    val marginSymbol = marginSymbol ?: ""
    val indentBeforeSymbol = (indentation - 1).coerceAtLeast(0)
    val indentAfterSymbol = 1
    return lines.map {
        """<div style="padding-left: ${indentBeforeSymbol}em;">$marginSymbol<span style="padding-left: ${indentAfterSymbol}em;">$it</div>"""
    }
}

fun bold(item: String): String {
    return "<b>$item</b>"
}

fun underline(item: String): String {
    return "<u>$item</u>"
}

fun indentLinesRoot(
    indentation: Int,
    item: String,
    marginSymbol: String? = null,
): List<String> {
    return indentLinesRoot(indentation, listOf(item), marginSymbol)
}
