package model

import model.utils.RecursionProtector
import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.atoms.Transition
import ru.misterpotz.model.collections.PetriAtomRegistry

interface PetriAtomVisitorDFS {

    fun visitArc(arc: Arc)

    fun visitTransition(transition: Transition)

    fun visitPlace(place: Place)

    fun cleanStack()
}

abstract class AbsPetriAtomVisitorDFS constructor(
    private val petriAtomRegistry: PetriAtomRegistry
) : PetriAtomVisitorDFS {
    protected val recursionProtector = RecursionProtector()

    final override fun visitArc(arc: Arc) {
        recursionProtector.protectWithRecursionStack(arc) {
            val canStopParsing = doForArcBeforeDFS(arc)
            if (!canStopParsing) {
                petriAtomRegistry[arc.arrowNodeId!!].acceptVisitor(this)
            }
            doForAtomAfterDFS(arc)
        }
    }

    final override fun visitTransition(transition: Transition) {
        recursionProtector.protectWithRecursionStack(transition) {
            val canStopParsing = doForTransitionBeforeDFS(transition)
            if (!canStopParsing) {
                for (outputPlace in transition.outputPlaces) {
                    val arc = with(petriAtomRegistry) {
                        transition.id.arcTo(outputPlace)
                    }
                    visitArc(arc)
                }
            }
            doForAtomAfterDFS(transition)
        }
    }

    final override fun visitPlace(place: Place) {
        recursionProtector.protectWithRecursionStack(place) {
            val canStopParsing = doForPlaceBeforeDFS(place)
            if (!canStopParsing) {
                for (toTransition in place.toTransitions) {
                    val arc = with(petriAtomRegistry) {
                        place.id.arcTo(toTransition)
                    }
                    visitArc(arc)
                }
            }
            doForAtomAfterDFS(place)
        }
    }

    open fun doForAtomAfterDFS(atom: PetriAtom) = Unit
    open fun doForArcBeforeDFS(arc: Arc) : Boolean = false
    open fun doForTransitionBeforeDFS(transition: Transition) : Boolean = false
    open fun doForPlaceBeforeDFS(place: Place) : Boolean = false

    override fun cleanStack() {
        recursionProtector.clean()
    }
}

interface ConsistencyCheckable {
//    fun check(consistencyCheckable: ConsistencyCheckable?,
//              visited: MutableSet<ConsistencyCheckable>,
//              recStack: MutableSet<ConsistencyCheckable>) : Boolean

//    companion object {
//        private fun isCyclicUtil(
//            consistencyCheckable: ConsistencyCheckable,
//            visited: MutableSet<ConsistencyCheckable>,
//            recStack: MutableSet<ConsistencyCheckable>,
//        ): Boolean {
//            // Mark the current node as visited and
//            // part of recursion stack
//            if (recStack.contains(consistencyCheckable)) return true
//            if (visited.contains(consistencyCheckable)) return false
//            visited.add(consistencyCheckable)
//            recStack.add(consistencyCheckable)
//
//
//            for (c in children) if (isCyclicUtil(c, visited, recStack)) return true
//            recStack.remove(consistencyCheckable)
//            return false
//        }
//    }
}
