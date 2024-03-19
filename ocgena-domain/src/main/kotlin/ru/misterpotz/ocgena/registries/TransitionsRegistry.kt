package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

interface TransitionsRegistry : Iterable<Transition> {
    operator fun get(transition: PetriAtomId): Transition
    val iterable: Iterable<Transition>
    val size: Int
}

fun TransitionsRegistry(petriAtomRegistry: PetriAtomRegistry): TransitionsRegistry {
    return TransitionsRegistryMap(petriAtomRegistry)
}

internal class TransitionsRegistryMap(
    private val petriAtomRegistry: PetriAtomRegistry,
) : TransitionsRegistry {
    override operator fun get(transition: PetriAtomId): Transition {
        return petriAtomRegistry[transition] as Transition
    }

    override val iterable: List<Transition> by lazy(LazyThreadSafetyMode.NONE) {
        petriAtomRegistry.getTransitions().map { petriAtomRegistry.getTransition(it) }
    }
    override val size: Int
        get() = iterable.size

    override fun iterator(): Iterator<Transition> {
        return iterable.iterator()
    }
}
