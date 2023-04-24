package simulation.binding

import model.ActiveFiringTransition
import model.ObjectMarking
import model.Place
import model.Transition

interface InputToOutputPlaceResolver {
    fun createOutputMarking(activeFiringTransition : ActiveFiringTransition) : ObjectMarking
}
