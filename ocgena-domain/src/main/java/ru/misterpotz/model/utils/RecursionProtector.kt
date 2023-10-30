package model.utils

import model.PetriAtom

class RecursionProtector() {
    val visitedSet: MutableSet<PetriAtom> = mutableSetOf()
    val recursiveStack: MutableList<PetriAtom> = mutableListOf()

    fun protectWithRecursionStack(petriAtom: PetriAtom, block: () -> Unit) {
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
