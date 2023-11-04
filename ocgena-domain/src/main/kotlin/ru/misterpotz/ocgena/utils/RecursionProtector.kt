package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

class RecursionProtector() {
    val visitedSet: MutableSet<PetriAtomId> = mutableSetOf()
    val recursiveStack: MutableList<PetriAtomId> = mutableListOf()

    fun protectWithRecursionStack(petriAtom: PetriAtomId, block: () -> Unit) {
        val recursiveStack = recursiveStack
        val visitedSet = visitedSet

        if (recursiveStack.contains(petriAtom)) {
            return
        }
        if (visitedSet.contains(petriAtom)) {
            return
        }

        visitedSet.add(petriAtom)
        recursiveStack.add(petriAtom)

        block()

        recursiveStack.remove(petriAtom)
    }

    fun clean() {
        // is not in process state
        require(recursiveStack.isEmpty())
        visitedSet.clear()
    }
}
