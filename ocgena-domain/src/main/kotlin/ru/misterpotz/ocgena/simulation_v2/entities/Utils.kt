package ru.misterpotz.ocgena.simulation_v2.entities

class Transitions(val transitions: List<TransitionWrapper>) : List<TransitionWrapper> by transitions {
    val map by lazy(LazyThreadSafetyMode.NONE) {
        transitions.associateBy { it.transitionId }
    }
}

fun List<TransitionWrapper>.wrap() = Transitions(this)
fun List<PlaceWrapper>.wrap() = Places(this)

class Places(val places: List<PlaceWrapper>) : List<PlaceWrapper> by places {
    val map = places.associateBy { it.placeId }
}