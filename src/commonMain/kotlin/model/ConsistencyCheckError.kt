package model

enum class ErrorLevel {
    WARNING,
    CRITICAL
}

sealed class ConsistencyCheckError(
    val level: ErrorLevel,
) {
    // STRUCTURE ---
    data class IsolatedSubgraphsDetected(
        val isolatedAtoms : List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.WARNING)
    object NoInputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL)
    object NoOutputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL)

    // TRANSITION ---
    data class MissingArc(
        val transition: Transition,
        val debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    // TODO: what about case, when there is only \mu arc connected to transition?
    data class VariableArcIsTheOnlyConnected(
        val transition: Transition,
        val debugPath: List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.WARNING)

    /**
     * well-formed condition (by Aalst) is not held
     */
    data class InconsistentVariabilityArcs(
        val transition: Transition,
        val inputArc: Arc,
        val outputArc: Arc,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    // PLACE ---
    data class MultipleArcsFromSinglePlaceToSingleTransition(
        val place : Place,
        val transition: Transition,
        val debugPath: List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    // TODO: can actually be checked when have set of all atoms
    data class IsolatedPlace(
        val place: Place,
        val debugPath : List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.WARNING)

    data class InputPlaceHasInputArcs(
        val place : Place,
        val debugPath : List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    data class OutputPlaceHasOutputArcs(
        val place : Place,
        val debugPath : List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    // ARC ---
    data class MissingNode(
        val arc: Arc,
        val debugPath: List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    data class IsNotBipartite(
        val arc: Arc,
        val debugPath: List<PetriAtom>
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)

    data class ArcInputEqualsOutput(
        val arc: Arc,
        val debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL)
}
