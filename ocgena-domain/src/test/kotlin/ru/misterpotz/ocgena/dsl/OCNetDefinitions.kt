package ru.misterpotz.ocgena.dsl

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.ocgena.utils.toIds

fun createExampleInputOutputPlaces(): PlaceTypeRegistry {
    return PlaceTypeRegistry.build {
        inputPlaces("o1 i1 r1".toIds())
        outputPlaces("o5 i6 r3".toIds())
    }
}

/**
 * @see <img src="src/jvmTest/resources/img.png" >
 */
fun createExampleModel(): OCNet {
    return OCNetBuilder().defineAtoms {
        // places and objects
        for (i in 1..5) {
            "o$i".p {
                objectTypeId = "order"
            }
        }
        for (i in 1..6) {
            "i$i".p {
                objectTypeId = "item"
            }
        }
        for (i in 1..3) {
            "r$i".p {
                objectTypeId = "route"
            }
        }
        // inputs and outputs
        "o1".p { input }
        "i1".p { input }
        "r1".p { input }

        "o5".p { output }
        "i6".p { output }
        "r3".p { output }

        // transitions
        val placeOrder = "place order".t
        val sendInvoice = "send invoice".t
        val pickItem = "pick item".t
        val startRoute = "start route".t
        val endRoute = "end route".t
        val markCompl = "mark as completed".t
        val payOrder = "pay order".t
        val sendReminder = "send reminder".t

        // arcs
        "o1".arc(placeOrder)
            .arc("o2")
            .arc(sendInvoice)
            .arc("o3")
            .also { o3 ->
                o3.arc(sendReminder)
                    .arc(o3)
            }
            .arc(payOrder)
            .arc("o4")
            .arc(markCompl)
            .arc("o5")

        "i1".arc(placeOrder) { vari }
            .arc("i2") { vari }
            .arc(pickItem)
            .arc("i3")
            .arc(startRoute) { vari }
            .arc("i4") { vari }
            .arc(endRoute) { vari }
            .arc("i5") { vari }
            .arc(markCompl) { vari }
            .arc("i6") { vari }

        "r1".arc(startRoute)
            .arc("r2")
            .arc(endRoute)
            .arc("r3")
    }
}
