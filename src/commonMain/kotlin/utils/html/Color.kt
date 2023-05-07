package utils.html

fun color(
    string: String,
    fontColor: String? = null,
    backgroundColor: String? = null
): String {
    val fontColorStr = fontColor?.let {
        "color: $fontColor;"
    } ?: ""
    val backroundColorStr = backgroundColor?.let {
        "background-color: $backgroundColor;"
    } ?: ""
    return """<span style="$fontColorStr $backroundColorStr">$string</span>"""
}
