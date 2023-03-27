import ast.ParseOption__0
import ast.Types
import ast.parse
import kotlinx.js.jso

//fun getMyObject() : nibba {
//    return js("nibba") as nibba
//}

fun Any.stringify(): String {
    return JSON.stringify(this)
}

fun main() {
//    val container = document.createElement("div")
//    document.body!!.appendChild(container)
//
//    val welcome = Welcome.create {
//        name = "Kotlin/JS"
//    }
//    createRoot(container).render(welcome)
//    bro()
    console.log("im running mat' tvoyu")
//    console.log("checking empty object on being asat node ${isASTBaseNode(js("""{ type: "biba boba 34" }"""))}")
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
    console.log("loggin choto : ${JSON.stringify(choto)}")
}
