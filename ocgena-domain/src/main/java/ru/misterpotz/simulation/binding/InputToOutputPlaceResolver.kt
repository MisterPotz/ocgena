package simulation.binding

import model.OngoingActivity
import ru.misterpotz.model.marking.ImmutableObjectMarking

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : OngoingActivity) : ImmutableObjectMarking
}
