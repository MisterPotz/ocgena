package model

import converter.OCNetElements
import dsl.ArcDSL
import dsl.NodeDSL
import dsl.NormalArcDSL
import dsl.OCScopeImpl
import dsl.PlaceDSL
import dsl.TransitionDSL
import dsl.VariableArcDSL

class OCNetDSLConverter(
    private val ocNetOCScopeImpl: OCScopeImpl,
) {
    private val foundObjectTypes: MutableMap<Int, ObjectType> = mutableMapOf()

    private val createdPlaces: MutableList<Place> = mutableListOf()
    private val createdTransitions: MutableList<Transition> = mutableListOf()
    private val createdArcs : MutableList<Arc> = mutableListOf()

    private fun getOrCreateTransition(transitionDSL: TransitionDSL): Transition {
        return createdTransitions.find { transitionDSL.label == it.label }
            ?: Transition(
                id = transitionDSL.label,
                label = transitionDSL.label,
                inputArcs = mutableListOf(),
                outputArcs = mutableListOf(),
            ).also {
                createdTransitions.add(it)
            }
    }

    private fun getOrCreatePlace(placeDSL: PlaceDSL): Place {
        return createdPlaces.find { placeDSL.label == it.label }
            ?: run {
                val objectType = foundObjectTypes.getOrPut(placeDSL.objectType.id) {
                    ObjectType(id = placeDSL.objectType.id, label = placeDSL.objectType.label)
                }

                Place(
                    label = placeDSL.label,
                    type = objectType,
                    placeType = placeDSL.placeType,
                    inputArcs = mutableListOf(),
                    outputArcs = mutableListOf(),
                    id = placeDSL.label,
                ).also {
                    it.initialTokens = placeDSL.initialTokens
                    createdPlaces.add(it)
                }
            }
    }

    private fun createArcId(node1 : LabelHolder?, node2: LabelHolder?) : String {

        return "${node1?.label}_${node2?.label}"
    }

    private fun createConnectedPlaceAndTransition(
        arcDSL: ArcDSL,
    ) {

        val node1DSL : NodeDSL? = (arcDSL.tailAtom as? PlaceDSL) ?: (arcDSL.tailAtom as? TransitionDSL)
        val node2DSL : NodeDSL? =  (arcDSL.arrowAtom as? PlaceDSL) ?: (arcDSL.arrowAtom as? TransitionDSL)

        // even if both dsl models are null, that would be probably be revealed by subsequent consistency check
        val arcIsInputToNode1 = node1DSL?.let { arcDSL.isInputFor(node1DSL) } ?: true

        val node1 = when (node1DSL) {
            is PlaceDSL -> getOrCreatePlace(node1DSL)
            is TransitionDSL -> getOrCreateTransition(node1DSL)
            else -> throw IllegalStateException("not supported")
        }
        val node2 = when (node2DSL) {
            is PlaceDSL -> getOrCreatePlace(node2DSL)
            is TransitionDSL -> getOrCreateTransition(node2DSL)
            else -> throw IllegalStateException("not supported")
        }

        val arrowNode = if (arcIsInputToNode1) {
            node1
        } else {
            node2
        }

        val tailNode = if (arcIsInputToNode1) {
            node2
        } else {
            node1
        }
        val arc = when (arcDSL) {
            is VariableArcDSL -> {
                VariableArc(
                    arrowNode = arrowNode,
                    tailNode = tailNode,
                    id = createArcId(tailNode, arrowNode)
                )
            }

            is NormalArcDSL -> {
                NormalArc(
                    arrowNode = arrowNode,
                    tailNode = tailNode,
                    multiplicity = arcDSL.multiplicity,
                    id = createArcId(tailNode, arrowNode)
                )
            }

            else -> return
        }

        if (arcIsInputToNode1) {
            node2.outputArcs.add(arc)
            node1.inputArcs.add(arc)
        } else {
            node1.outputArcs.add(arc)
            node2.inputArcs.add(arc)
        }
        createdArcs.add(arc)
    }

    fun convert(): Output {
        for (arcDSL in ocNetOCScopeImpl.arcs) {
            createConnectedPlaceAndTransition(arcDSL)
        }
        // if arcs were not connected to some defined places or transitions,
        // they weren't created before
        for (placeDSL in ocNetOCScopeImpl.places.values) {
            getOrCreatePlace(placeDSL)
        }
        for (transitionDSL in ocNetOCScopeImpl.transitions.values) {
            getOrCreateTransition(transitionDSL)
        }
        return Output(
            places = createdPlaces,
            transitions = createdTransitions,
            arcs = createdArcs,
            allPetriNodes = buildList {
                addAll(createdPlaces)
                addAll(createdTransitions)
            }
        )
    }

    class Output(
        override val places : List<Place>,
        override val transitions: List<Transition>,
        override val arcs : List<Arc>,
        override val allPetriNodes : List<PetriNode>
    ): OCNetElements {
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
}
