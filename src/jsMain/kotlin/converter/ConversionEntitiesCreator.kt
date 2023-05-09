package converter

import model.*

class ConversionEntitiesCreator(val placeTyping: PlaceTyping) {
    private val places: MutableMap<PlaceId, Place> = mutableMapOf()
    private val transition: MutableMap<TransitionId, Transition> = mutableMapOf()
    private val arcs: MutableList<Arc> = mutableListOf()
    fun recordPlace(key: String): Place {
        return places.getOrPut(key) {
            Place(
                id = key,
                label = key,
            )
        }
    }

    fun recordTransition(key: String): Transition {
        return transition.getOrPut(key) {
            Transition(
                id = key,
                label = key,
            )
        }
    }

    fun recordArc(arc: Arc) {
        arcs.add(arc)
    }

    fun buildOcNetElements(): OCNetElements {
        return OCNetElementsImpl(
            places = Places(places.values.toList()),
            transitions = Transitions(transition.values.toList()),
            arcs = Arcs(),
            objectTypes = ObjectTypes(placeTyping.toObjectTypes()),
            placeTyping = placeTyping
        )
    }

    fun elementByLabel(node: String): PetriNode? {
        return places[node] ?: transition[node]
    }

}
