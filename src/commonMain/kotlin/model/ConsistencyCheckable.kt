package model

import model.utils.RecursionProtector

interface PetriAtomVisitorDFS {

    fun visitArc(arc: Arc)

    fun visitTransition(transition: Transition)

    fun visitPlace(place: Place)

    fun cleanStack()
}

abstract class AbsPetriAtomVisitorDFS : PetriAtomVisitorDFS {
    protected val recursionProtector = RecursionProtector()

    final override fun visitArc(arc: Arc) {
        recursionProtector.protectWithRecursionStack(arc) {
            val canStopParsing = doForArcBeforeDFS(arc)
            if (!canStopParsing) {
                arc.arrowNode!!.acceptVisitor(this)
            }
            doForAtomAfterDFS(arc)
        }
    }

    final override fun visitTransition(transition: Transition) {
        recursionProtector.protectWithRecursionStack(transition) {
            val canStopParsing = doForTransitionBeforeDFS(transition)
            if (!canStopParsing) {
                for (outputArc in transition.outputArcs) {
                    visitArc(outputArc)
                }
            }
            doForAtomAfterDFS(transition)
        }
    }

    final override fun visitPlace(place: Place) {
        recursionProtector.protectWithRecursionStack(place) {
            val canStopParsing = doForPlaceBeforeDFS(place)
            if (!canStopParsing) {
                for (outputArc in place.outputArcs) {
                    visitArc(outputArc)
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
