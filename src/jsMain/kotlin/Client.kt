@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import ast.Types
import converter.OCDotParser
import kotlinx.js.jso


fun Any.stringify(): String {
    return JSON.stringify(this)
}

fun probeParse() : dynamic {
//    val container = document.createElement("div")
//    document.body!!.appendChild(containper)
//
//    val welcome = Welcome.create {
//        name = "Kotlin/JS"
//    }

    val ocDot = """
        |ocnet { 
        |   transitions { 
        |       t1
        |       t2
        |       t3
        |   }
        |   places { 
        |       p1
        |       p2
        |       p3
        |   }
        |       
        |   p1 => t1 -> p2
        |}
    """.trimMargin()

    val types = Types.OcDot
    console.log("OcDot: $types")
    val kek = jso<dynamic>()
    kek["name"] = "bro"

    console.log(kek)
    console.log(kek["name"])
    
    val parser = OCDotParser()
    parser.parse(ocDot)



//    val choto = parse(ocDot, rule)
//    val ocNetFacade = OCNetFacadeBuilder()
//    val builtModel = ocNetFacade.tryBuildModel {
//        place("t1")
//            .variableArcTo(transition("t1"))
//            .arcTo(place("p2"))
//    }
    return jso()
}

fun main() {
    probeParse()
}
