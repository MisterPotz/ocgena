@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import ast.ParseOption__0
import ast.Types
import ast.parse
import dsl.OCNetFacadeBuilder
import kotlinx.js.jso

fun Any.stringify(): String {
    return JSON.stringify(this)
}

fun probeParse() : dynamic {
//    val container = document.createElement("div")
//    document.body!!.appendChild(container)
//
//    val welcome = Welcome.create {
//        name = "Kotlin/JS"
//    }
    val ocDot = """
        |ocnet { 
        |    p1
        |    t1
        |    p1 => t1 -> p2
        |}
    """.trimMargin()
    val types = Types
    console.log("OcDot: ${types.OcDot}")
    val rule = jso<ParseOption__0> {
        rule = Types.OcDot
    }
    val choto = parse(ocDot, rule)
    val ocNetFacade = OCNetFacadeBuilder()
    val builtModel = ocNetFacade.tryBuildModel {
        place("t1")
            .variableArcTo(transition("t1"))
            .arcTo(place("p2"))
    }
    console.log(builtModel.ocNet?.toString())
    return choto
}

fun main() {

}
