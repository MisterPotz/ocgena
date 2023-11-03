package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

interface TransitionsRegistry {
    operator fun get(transition: PetriAtomId) : Transition
    val iterable : Iterable<Transition>
}

fun TransitionsRegistry(petriAtomRegistry: PetriAtomRegistry) : TransitionsRegistry {
    return TransitionsRegistryMap(petriAtomRegistry)
}

internal class TransitionsRegistryMap(
    private val petriAtomRegistry: PetriAtomRegistry,
) : TransitionsRegistry {
    override operator fun get(transition: PetriAtomId): Transition {
        return petriAtomRegistry[transition] as Transition
    }

    override val iterable: Iterable<Transition>
        get() = petriAtomRegistry.getTransitions().map { petriAtomRegistry.getTransition(it) }
}
