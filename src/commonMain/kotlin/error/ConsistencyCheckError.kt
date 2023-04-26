package error

import model.Arc
import model.PetriAtom
import model.Place
import model.Transition


fun <T : Any> List<T>.prettyPrint() : String {
    return joinToString("\n").prependIndent()
}

sealed class ConsistencyCheckError(
    override val errorLevel: ErrorLevel,
    val debugPath: List<PetriAtom>?,
    val arcs: List<Arc>? = null,
    val transition: Transition? = null,
    val place: Place? = null,
) : Error {

    // STRUCTURE ---
    class IsolatedSubgraphsDetected(
    ) : ConsistencyCheckError(ErrorLevel.WARNING, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String = "Isolated subgraphs present in the node (not fully reachable graph)"
        override val errorLevel: ErrorLevel = ErrorLevel.WARNING
    }

    object NoInputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "No input places present in the net"
    }

    object NoOutputPlacesDetected : ConsistencyCheckError(ErrorLevel.CRITICAL, null) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "No output places present in the net"
    }

    // TRANSITION ---
    class MissingArc(
        transition: Transition,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, transition = transition) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "Transition misses one of the arcs"


    }

    // TODO: what about case, when there is only \mu arc connected to transition?
    class VariableArcIsTheOnlyConnected(
        transition: Transition,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.WARNING, debugPath, transition = transition) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "Variable arc is the only connected to the node, as output or input"
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

        override val message: String
            get() = "Variable arcs inconsistenct: among arcs to the node are both variable and normal for one type"
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

        override val message: String
            get() = "Multiple arcs from a place to the same transition"
    }

    // TODO: can actually be checked when have set of all atoms
    class IsolatedPlace(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.WARNING, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "Isolated places subgraphs detected (not connected to other nodes"
    }

    class InputPlaceHasInputArcs(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "input place contains prohibited input arcs"
    }

    class OutputPlaceHasOutputArcs(
        place: Place,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath, place = place) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "output place contains prohibited output arcs"
    }

    // ARC ---
    class MissingNode(
        val arc: Arc,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "arc misses one of the connected nodes"
    }

    class IsNotBipartite(
        val arc: Arc,
        debugPath: List<PetriAtom>,
    ) : ConsistencyCheckError(ErrorLevel.CRITICAL, debugPath) {
        override fun label(): String {
            return this::class.simpleName!!
        }

        override val message: String
            get() = "is not bipartite graph ($debugPath)"
        override val errorLevel: ErrorLevel
            get() = ErrorLevel.CRITICAL
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

        override val message: String
            get() = "arc is linked to the same node it comes from"
    }

    abstract fun label(): String

    override fun toString(): String {
        return """
            ${label()}(
                level: $errorLevel
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
