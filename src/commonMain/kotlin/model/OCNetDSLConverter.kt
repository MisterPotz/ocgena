package model

import dsl.ArcDSL
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
                label = transitionDSL.label,
                _inputArcs = mutableListOf(),
                _outputArcs = mutableListOf(),
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
                    _inputArcs = mutableListOf(),
                    _outputArcs = mutableListOf()
                ).also {
                    createdPlaces.add(it)
                }
            }
    }

    private fun createConnectedPlaceAndTransition(
        arcDSL: ArcDSL,
    ) {
        val placeDSL = (arcDSL.tailAtom as? PlaceDSL) ?: (arcDSL.arrowAtom as? PlaceDSL)
        val transitionDSL = (arcDSL.tailAtom as? TransitionDSL) ?: (arcDSL.arrowAtom as? TransitionDSL)

        // even if both dsl models are null, that would be probably be revealed by subsequent consistency check
        val arcIsInputToPlace = placeDSL?.let { arcDSL.isInputFor(placeDSL) } ?: true

        val place = placeDSL?.let { getOrCreatePlace(it) }
        val transition = transitionDSL?.let { getOrCreateTransition(it) }

        val arrowNode = if (arcIsInputToPlace) {
            place
        } else {
            transition
        }

        val tailNode = if (arcIsInputToPlace) {
            transition
        } else {
            place
        }
        val arc = when (arcDSL) {
            is VariableArcDSL -> {
                VariableArc(
                    arrowNode = arrowNode,
                    tailNode = tailNode
                )
            }

            is NormalArcDSL -> {
                NormalArc(
                    arrowNode = arrowNode,
                    tailNode = tailNode,
                    multiplicity = arcDSL.multiplicity
                )
            }

            else -> return
        }

        if (arcIsInputToPlace) {
            transition?._outputArcs?.add(arc)
            place?._inputArcs?.add(arc)
        } else {
            place?._outputArcs?.add(arc)
            transition?._inputArcs?.add(arc)
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
        val places : List<Place>,
        val transitions: List<Transition>,
        val arcs : List<Arc>,
        val allPetriNodes : List<PetriNode>
    ) {
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
