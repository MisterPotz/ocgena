package ru.misterpotz.ocgena.dsl

import dsl.NodeDSL
import dsl.PlaceDSL
import dsl.SubgraphDSL
import dsl.TransitionDSL

class EntitiesCreatedInSubgraph(
    val places: MutableMap<String, PlaceDSL> = mutableMapOf(),
    val transitions: MutableMap<String, TransitionDSL> = mutableMapOf(),
    val subgraphs: MutableMap<String, SubgraphDSL> = mutableMapOf(),
) {

    fun getAllNodes() : List<NodeDSL> {
        return buildList {
            addAll(places.values)
            addAll(transitions.values)
        }
    }

    fun recordCreatedPlace(placeDSL: PlaceDSL) {
        places[placeDSL.label] = placeDSL
    }

    fun recordCreatedTransition(transitionDSL: TransitionDSL) {
        transitions[transitionDSL.label] = transitionDSL
    }

    fun recordCreatedSubgraph(subgraphDSL: SubgraphDSL) {
        subgraphs[subgraphDSL.label] = subgraphDSL
    }
}
