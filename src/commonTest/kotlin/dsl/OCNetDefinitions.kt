package dsl

import model.InputOutputPlaces
import model.PlaceTyping
import utils.toIds

fun createExamplePlaceTyping() : PlaceTyping {
    return PlaceTyping.build {
        objectType("order", "o1 o2 o3 o4 o5")
        objectType("item", "i1 i2 i3 i4 i5 i6")
        objectType("route", "r1 r2 r3")
    }
}

fun createExampleInputOutputPlaces() : InputOutputPlaces {
    return InputOutputPlaces.build {
        inputPlaces("o1 i1 r1".toIds())
        outputPlaces("o5 i6 r3".toIds())
    }
}

/**
 * @see <img src="src/jvmTest/resources/img.png" >
 */
fun createExampleModel(): OCNetDSLElements {
    val ocNet = OCNetBuilder.define {
        val order = objectType("order") {
            "o$it"
        }
        val item = objectType("item")
        val route = objectType("route") {
            "r$it"
        }

        forType(order) {
            place("o1").arcTo(transition("place order"))
                .arcTo(place("o2"))
                .arcTo(transition("send invoice"))
                .connectTo(subgraph {
                    val place = place("o3")
                    inNode
                        .arcTo(place)
                        .arcTo(transition("send reminder"))
                        .arcTo(place)
                        .arcTo(outNode)
                })
                .connectTo(transition("pay order"))
                .arcTo(place("o4"))
                .arcTo(transition("mark as completed"))
                .arcTo(place("o5"))
        }

        forType(item) {
            place("i1")
                .variableArcTo(transition("place order"))
                .variableArcTo(place("i2"))
                .arcTo(transition("pick item"))
                .arcTo(place("i3"))
                .variableArcTo(transition("start route"))
                .variableArcTo(place("i4"))
                .variableArcTo(transition("end route"))
                .variableArcTo(place("i5"))
                .variableArcTo(transition("mark as completed"))
                .variableArcTo(place("i6"))
        }

        forType(route) {
            place("r1").arcTo(transition("start route"))
                .arcTo(place("r2"))
                .arcTo(transition("end route"))
                .arcTo(place("r3"))
        }
    }
    return ocNet
}
