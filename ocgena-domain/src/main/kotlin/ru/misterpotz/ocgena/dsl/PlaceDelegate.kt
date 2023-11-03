package dsl

import ru.misterpotz.ocgena.dsl.*

open class PlaceDelegate(
    val placeCreator: PlaceCreator,
) : PlaceAcceptor {
    override fun place(label: String): PlaceDSL {
        return placeCreator.createPlace(label) { }
    }

    override fun place(block: OCPlaceScope.() -> Unit): PlaceDSL {
        return placeCreator.createPlace(label = null, block)
    }

    override fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL {
        return placeCreator.createPlace(label, block)
    }
}

class SubgraphPlaceDelegate(
    private val entitiesCreatedInSubgraph: EntitiesCreatedInSubgraph,
    placeCreator: PlaceCreator,
) : PlaceDelegate(placeCreator) {

    override fun place(label: String): PlaceDSL {
        val newPlaceDSL = super.place(label)
        entitiesCreatedInSubgraph.recordCreatedPlace(newPlaceDSL)
        return newPlaceDSL
    }

    override fun place(block: OCPlaceScope.() -> Unit): PlaceDSL {
        val newPlaceDSL = super.place(block)
        entitiesCreatedInSubgraph.recordCreatedPlace(newPlaceDSL)
        return newPlaceDSL
    }

    override fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL {
        val newPlaceDSL = super.place(label, block)
        entitiesCreatedInSubgraph.recordCreatedPlace(newPlaceDSL)
        return newPlaceDSL
    }
}

class SubgraphTransitionDelegate(
    private val entitiesCreatedInSubgraph: EntitiesCreatedInSubgraph,
    transitionCreator: TransitionCreator,
) : TransitionDelegate(transitionCreator) {
    override fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL {
        return super.transition(block).also {
            entitiesCreatedInSubgraph.recordCreatedTransition(it)
        }
    }

    override fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL {
        return super.transition(label, block).also {
            entitiesCreatedInSubgraph.recordCreatedTransition(it)
        }
    }

    override fun transition(label: String): TransitionDSL {
        return super.transition(label).also {
            entitiesCreatedInSubgraph.recordCreatedTransition(it)
        }
    }
}
