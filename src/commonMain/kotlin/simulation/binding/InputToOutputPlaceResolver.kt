package simulation.binding

import model.ActiveFiringTransition
import model.ImmutableObjectMarking

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : ActiveFiringTransition) : ImmutableObjectMarking
}
