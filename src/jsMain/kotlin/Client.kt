@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import ast.Types
import converter.OCDotParser
import kotlinx.js.jso
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


fun Any.stringify(): String {
    return JSON.stringify(this)
}

@OptIn(ExperimentalTime::class)
fun probeParse() : dynamic {
    val ocDot = """
        |ocnet { 
        |   transitions { 
        |       t1
        |   }
        |   places { 
        |       p1
        |       p2
        |   }
        | 
        |   
        |       
        |   p1 => t1 -> p2
        |}
    """.trimMargin()
    val parser = OCDotParser()
    val timeTakenForThis = measureTime {
        parser.parse(ocDot)
    }.inWholeMilliseconds
    console.log(timeTakenForThis)
    return jso()
}

fun main() {
    probeParse()
}
