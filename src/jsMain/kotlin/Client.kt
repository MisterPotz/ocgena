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
        |       p3
        |   }
        |   
        |    // seems not necessary
        |    /* object types { 
        |       type1 type2
        |   } */
        |   
        |   places for type1 {
        |       p1
        |   }
        |       
        |   places for type2 {
        |       p2 p3
        |   }
        | 
        |   inputs {
        |       p1 p3
        |   }
        |      
        |   outputs {
        |       p2
        |   }
        | 
        |   p1 => t1 -> p2
        |   p3 2-> t1
        |  
        |
        |   initial marking {
        |       p1=10
        |       p3=8
        |   }
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
//    probeParse()
}
