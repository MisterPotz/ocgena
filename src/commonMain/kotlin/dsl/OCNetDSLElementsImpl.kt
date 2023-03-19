package dsl

class OCNetDSLElementsImpl(
    override val places : MutableMap<String, PlaceDSL>,
    override val transitions: Map<String, TransitionDSL>,
    override val arcs : MutableList<ArcDSL>,
    override val objectTypes: Map<String, ObjectTypeDSL>, override val defaultObjectTypeDSL: ObjectTypeDSL,
): OCNetDSLElements {
    override fun objectType(s: String): ObjectTypeDSL {
        return objectTypes[s]!!
    }

    override fun transition(s: String): NodeDSL {
        return transitions[s]!!
    }

    override fun place(s: String): NodeDSL {
        return places[s]!!
    }

    override val allPetriNodes : List<NodeDSL> = buildList {
        addAll(places.values)
        addAll(transitions.values)
    }

    override fun toString(): String {
        return """
            |Output(
            |   places: $places
            |   transitions: $transitions,
            |   arcs: $arcs
            |)
        """.trimMargin()
    }
}

class ObjectsSearcher(private val ocNetDSLElements: OCNetDSLElements) {

    fun withoutDefaultObjectTypeIfPossible(): Map<String, ObjectTypeDSL> {
        return if (ocNetDSLElements.objectTypes.size == 1) {
            ocNetDSLElements.objectTypes
        } else {
            ocNetDSLElements.objectTypes.toMutableMap().apply {
                if (!defaultObjectTypeWasUsed(ocNetDSLElements.defaultObjectTypeDSL, ocNetDSLElements)) {
                    remove(ocNetDSLElements.defaultObjectTypeDSL.label)
                }
            }
        }
    }

    private fun defaultObjectTypeWasUsed(defaultObjectTypeDSL: ObjectTypeDSL, placeContainer: PlacesContainer): Boolean {
        return placeContainer.places.values.find { it.objectType == defaultObjectTypeDSL } != null
    }
}
