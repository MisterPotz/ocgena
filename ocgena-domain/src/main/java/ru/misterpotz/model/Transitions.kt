package model

import ru.misterpotz.model.atoms.Transition
import ru.misterpotz.model.atoms.TransitionId


interface Transitions {
    operator fun get(transition: PetriAtomId) : Transition
}

fun Transitions(transitions: Map<TransitionId, Transition>) : Transitions {
    return TransitionsMap(transitions)
}

internal class TransitionsMap(
    val transitions: Map<PetriAtomId, Transition>
) : Transitions {
    override operator fun get(transition: PetriAtomId): Transition {
        return transitions[transition]!!
    }
}
