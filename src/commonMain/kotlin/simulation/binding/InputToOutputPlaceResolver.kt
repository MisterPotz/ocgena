package simulation.binding

import model.Place
import model.Transition

interface InputToOutputPlaceResolver {
    fun getOutputPlaceForInput(transition : Transition, inputPlace : Place) : Place
}
