package model

interface ParsingConsistencyCheckVisitor {

    fun checkConsistencyForArc(arc: Arc)

    fun checkConsistencyForTransition(transition: Transition)

    fun checkConsistencyForPlace(place: Place)
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
