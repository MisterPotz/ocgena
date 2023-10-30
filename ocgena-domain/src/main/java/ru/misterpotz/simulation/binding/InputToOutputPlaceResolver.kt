package simulation.binding

import model.ActiveFiringTransition
import ru.misterpotz.model.ImmutableObjectMarking

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : ActiveFiringTransition) : ImmutableObjectMarking
}
