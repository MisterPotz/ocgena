package simulation.binding

import model.TransitionInstance
import ru.misterpotz.model.marking.ImmutableObjectMarking

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutableObjectMarking
}
