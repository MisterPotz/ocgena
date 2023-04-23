package model

import simulation.Marking

abstract class TimePetriNet {

    // static
    abstract val placesP: Places
    abstract val transitionsT: Transitions
    abstract val arcsF: Arcs

    // V:  val arc multiplicities - multiplicities of the arcs
    abstract val arcMultiplicitiesV: ArcMultiplicity
    val initialMarking: Marking = Marking()
    abstract val intervalFunctionI: IntervalFunction

    // dynamic
    abstract var state: State

    interface ArcMultiplicity {
        operator fun get(arc: Arc): Int
    }

    interface Arcs {
        operator fun get(place: Place): WithPlaceGetter
        operator fun get(transition: model.Transition): WithTransitionGetter

        interface WithPlaceGetter {
            operator fun get(transition: model.Transition): Arc?
        }

        interface WithTransitionGetter {
            operator fun get(place: Place): Arc?
        }
    }

    fun transitionReadyToFire(transition: model.Transition): Boolean {
        return transition.isBindingEnabled()
    }

    interface State {
        val pMarking: Marking
        val tMarking: TMarking
    }
}

