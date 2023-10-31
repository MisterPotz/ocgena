package simulation.binding

import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.marking.objects.ImmutableObjectMarking

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : TransitionInstance) : ImmutableObjectMarking
}
