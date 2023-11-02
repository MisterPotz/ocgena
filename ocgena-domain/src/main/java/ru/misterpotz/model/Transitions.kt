package model

import ru.misterpotz.model.atoms.Transition
import ru.misterpotz.model.atoms.TransitionId
import ru.misterpotz.model.collections.PetriAtomRegistry


interface Transitions {
    operator fun get(transition: PetriAtomId) : Transition
}

fun Transitions(petriAtomRegistry: PetriAtomRegistry) : Transitions {
    return TransitionsMap(petriAtomRegistry)
}

internal class TransitionsMap(
    private val petriAtomRegistry: PetriAtomRegistry,
) : Transitions {
    override operator fun get(transition: PetriAtomId): Transition {
        return petriAtomRegistry[transition] as Transition
    }
}
