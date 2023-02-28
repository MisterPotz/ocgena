package dsl

import model.PlaceType

/**
 * @see <img src="src/jvmTest/resources/img.png" >
 */
fun createExampleModel() : OCScopeImpl {
    return OCScopeImpl().apply {
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
            }.arcTo(transition("place order"))
                .arcTo(place { })
                .arcTo(transition("send invoice"))
                .arcTo(subgraph {
                    setAsInputOutput(place { })

                    input.arcTo(transition("send reminder"))
                    transition("send reminder").arcTo(input)
                })
                .arcTo(transition("pay order"))
                .arcTo(place { })
                .arcTo(transition("mark as completed"))
                .arcTo(place {
                    placeType = PlaceType.OUTPUT
                })
        }

        forType(item) {
            place {
                placeType = PlaceType.INPUT
            }
                .variableArcTo(transition("place order"))
                .variableArcTo(place { })
                .arcTo(transition("pick item"))
                .arcTo(place { })
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
            }.arcTo(transition("start route"))
                .arcTo(place { })
                .arcTo(transition("end route"))
                .arcTo(place {
                    placeType = PlaceType.OUTPUT
                })
        }
    }
}
