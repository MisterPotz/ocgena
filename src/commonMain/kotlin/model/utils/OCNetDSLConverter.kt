package model.utils

import dsl.ArcDSL
import dsl.NodeDSL
import dsl.NormalArcDSL
import dsl.OCNetDSLElements
import dsl.PlaceDSL
import dsl.TransitionDSL
import dsl.VariableArcDSL
import model.*
import model.typea.VariableArcTypeA

class OCNetDSLConverter(
    private val ocNetDSLElements: OCNetDSLElements,
    private val placeTyping: PlaceTyping,
) {
    private val foundObjectTypes: MutableMap<ObjectTypeId, ObjectType> = mutableMapOf()
    private val elementsIdCreator = ElementsIdCreator()
    private val createdPlaces: MutableList<Place> = mutableListOf()
    private val createdTransitions: MutableList<Transition> = mutableListOf()
    private val createdArcs : MutableList<Arc> = mutableListOf()

    private fun getOrCreateTransition(transitionDSL: TransitionDSL): Transition {
        return createdTransitions.find { transitionDSL.label == it.label }
            ?: Transition(
                id = elementsIdCreator.createTransitionId(transitionDSL),
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
//                val objectType = foundObjectTypes.getOrPut(placeDSL.objectType.id) {
//                    ObjectType(id = placeDSL.objectType.id, label = placeDSL.objectType.label)
//                }

                Place(
                    label = elementsIdCreator.createPlaceId(placeDSL),
//                    type = objectType,
                    inputArcs = mutableListOf(),
                    outputArcs = mutableListOf(),
                    id = placeDSL.label,
                ).also {
                    createdPlaces.add(it)
                }
            }
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
        val arcId = elementsIdCreator.createArcId(arcDSL)
        val arc = when (arcDSL) {
            is VariableArcDSL -> {
                VariableArcTypeA(
                    arrowNode = arrowNode,
                    tailNode = tailNode,
                    id = arcId
                )
            }

            is NormalArcDSL -> {
                NormalArc(
                    arrowNode = arrowNode,
                    tailNode = tailNode,
                    multiplicity = arcDSL.multiplicity,
                    id = arcId
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

    fun convert(): OCNetElementsImpl {
        for (arcDSL in ocNetDSLElements.arcs) {
            createConnectedPlaceAndTransition(arcDSL)
        }
        // if arcs were not connected to some defined places or transitions,
        // they weren't created before
        for (placeDSL in ocNetDSLElements.places.values) {
            getOrCreatePlace(placeDSL)
        }
        for (transitionDSL in ocNetDSLElements.transitions.values) {
            getOrCreateTransition(transitionDSL)
        }
        return OCNetElementsImpl(
            places = Places(createdPlaces),
            transitions = Transitions(createdTransitions),
            arcs = Arcs(),
            allArcs = createdArcs,
            objectTypes =  ObjectTypes(foundObjectTypes.values.toList()),
            placeTyping = placeTyping
        )
    }
}
