package model

enum class ErrorLevel {
    WARNING,
    CRITICAL
}

fun <T : Any> List<T>.prettyPrint() : String {
    return joinToString("\n").prependIndent()
}

sealed class ConsistencyCheckError(
    val level: ErrorLevel,
    val debugPath: List<PetriAtom>?,
    val arcs: List<Arc>? = null,
    val transition: Transition? = null,
    val place: Place? = null,
) {

    // STRUCTURE ---
    class IsolatedSubgraphsDetected(
    ) : ConsistencyCheckError(ErrorLevel.WARNING, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    object NoInputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    object NoOutputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    // TRANSITION ---
    class MissingArc(
        transition: Transition,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, transition = transition) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    // TODO: what about case, when there is only \mu arc connected to transition?
    class VariableArcIsTheOnlyConnected(
        transition: Transition,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.WARNING, debugPath, transition = transition) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    /**
     * well-formed condition (by Aalst) is not held
     */
    class InconsistentVariabilityArcs(
        transition: Transition,
        inputArc: Arc,
        outputArc: Arc,
    ) : ConsistencyCheckError(
        ErrorLevel.CRITICAL,
        debugPath = null,
        arcs = listOf(inputArc, outputArc),
        transition = transition
    ) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    // PLACE ---
    class MultipleArcsFromSinglePlaceToSingleTransition(
        place: Place,
        transition: Transition,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, place = place, transition = transition) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    // TODO: can actually be checked when have set of all atoms
    class IsolatedPlace(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.WARNING, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    class InputPlaceHasInputArcs(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    class OutputPlaceHasOutputArcs(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    // ARC ---
    class MissingNode(
        val arc: Arc,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    class IsNotBipartite(
        val arc: Arc,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath) {
        override fun label(): String {
            return this::class.simpleName!!
        }
    }

    class ArcInputEqualsOutput(
        val arc: Arc,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(
        ErrorLevel.CRITICAL,
        debugPath
    ) {

        override fun label(): String {
            return this::class.simpleName!!
        }

        override fun toString(): String {
            return "ArcInputEqualsOutput(arc=$arc)"
        }
    }

    abstract fun label(): String

    override fun toString(): String {
        return """
            ${label()}(
                level: $level
                path: ${debugPath?.let { toStringDebugPath(it) }}
                arcs: $arcs
                transition: $transition
                place: $place
            )
        """.trimIndent()
    }

    companion object {
        fun toStringDebugPath(debugPath: List<PetriAtom>): String {
            return debugPath.joinToString(separator = "\t")
        }
    }
}
