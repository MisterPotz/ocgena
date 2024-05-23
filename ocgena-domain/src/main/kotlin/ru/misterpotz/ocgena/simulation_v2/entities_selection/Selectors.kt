package ru.misterpotz.ocgena.simulation_v2.entities_selection

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.SynchronizedArcGroup

fun getTransitionPreplacesMap(
    ocNet: OCNet,
): Map<PetriAtomId, Set<PetriAtomId>> {
    return buildMap {
        for (transition in ocNet.transitionsRegistry.iterable) {
            val inputPlaces = transition.fromPlaces
            put(transition.id, inputPlaces.toSet())
        }
    }
}

fun getTransitionPostPlaces(
    ocNet: OCNet,
): Map<PetriAtomId, Set<PetriAtomId>> {
    return buildMap {
        for (transition in ocNet.transitionsRegistry.iterable) {
            val outputPlaces = transition.toPlaces
            put(transition.id, outputPlaces.toSet())
        }
    }
}

fun getTransitionsWithSharedPreplacesFor(ocNet: OCNet, targetTransition: PetriAtomId): List<PetriAtomId> {
    val share = mutableSetOf<PetriAtomId>()
    val preplacesOfThisTransition = ocNet.transitionsRegistry[targetTransition].fromPlaces.toSet()

    for (transition in ocNet.transitionsRegistry) {
        if (transition.fromPlaces.intersect(preplacesOfThisTransition).isNotEmpty()) {
            share.add(transition.id)
        }
    }
    return share.toList()
}

data class AffectedTransition(
    val transition: PetriAtomId,
    val middlemanPlaces: Set<PetriAtomId>
): Comparable<AffectedTransition> {
    override fun compareTo(other: AffectedTransition): Int {
        return transition.compareTo(other.transition)
    }
}

fun getDependentTransitions(ocNet: OCNet, targetTransition: PetriAtomId): List<AffectedTransition> {
    val set = mutableSetOf<PetriAtomId>()

    val preplaces = ocNet.transitionsRegistry[targetTransition].fromPlaces.toSet()
//    val postplaces = ocNet.transitionsRegistry[targetTransition].toPlaces.toSet()

    val allOtherTransitionsExceptThis =
        ocNet.transitionsRegistry.iterable.filter { it.id != targetTransition }

    return allOtherTransitionsExceptThis.mapNotNull {
        val sharedPreplaces = preplaces.intersect(it.fromPlaces.toSet())
//        val sharedPostplaces = postplaces.intersect(it.fromPlaces.toSet())

        if (sharedPreplaces.isNotEmpty()) {
            AffectedTransition(it.id, sharedPreplaces)
        } else {
            null
        }
    }
}

fun getSyncGroups(
    simulationInput: SimulationInput,
    targetTransition: PetriAtomId
): List<SynchronizedArcGroup>? {
    return simulationInput
        .transitions[targetTransition]
        ?.synchronizedArcGroups
}
