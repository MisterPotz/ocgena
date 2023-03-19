package dsl

import model.PlaceType

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
            place {
                placeType = PlaceType.INPUT
            }.arcTo(1, transition("place order"))
                .arcTo(1, place { })
                .arcTo(1, transition("send invoice"))
                .connectTo(
                    subgraph {
                        inNode
                            .arcTo(1, place { })
                            .arcTo(1, transition("send reminder"))
                            .arcTo(1, inNode)
                    })
            transition("send invoice")
                .arcTo(1, transition("pay order"))
                .arcTo(1, place { })
                .arcTo(1, transition("mark as completed"))
                .arcTo(1, place {
                    placeType = PlaceType.OUTPUT
                })
        }

        forType(item) {
            place {
                placeType = PlaceType.INPUT
            }
                .variableArcTo(transition("place order"))
                .variableArcTo(place { })
                .arcTo(1, transition("pick item"))
                .arcTo(1, place { })
                .variableArcTo(transition("start route"))
                .variableArcTo(place { })
                .variableArcTo(transition("end route"))
                .variableArcTo(place { })
                .variableArcTo(transition("mark as completed"))
                .variableArcTo(place {
                    placeType = PlaceType.OUTPUT
                })
        }

        forType(route) {
            place {
                placeType = PlaceType.INPUT
            }.arcTo(1, transition("start route"))
                .arcTo(1, place { })
                .arcTo(1, transition("end route"))
                .arcTo(1, place {
                    placeType = PlaceType.OUTPUT
                })
        }
    }
    return ocNet
}
