package converter

import ast.OcDot
import ast.ParseOption__0
import ast.Types
import kotlinx.js.jso

actual class OCDotParser {
    actual fun parse(ocDot: String): OCDotParseResult {
        val rule = jso<ParseOption__0> {
            rule = Types.OcDot
        }
        // always start parce from the root
        val parsedStructure = ast.parse(ocDot, rule) as OcDot

        val elemnts = parsedStructure
    }
}
