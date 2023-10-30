package model

class Arcs() {
    private val withPlaceGetter = WithPlaceGetter()
    private val withTransitionGetter = WithTransitionGetter()

    operator fun get(place: Place): WithPlaceGetter {
        return withPlaceGetter.also {
            it.place = place
        }
    }

    operator fun get(transition: Transition): WithTransitionGetter {
        return withTransitionGetter.also {
            it.transition = transition
        }
    }

    inner class WithPlaceGetter {
        var place: Place? = null

        operator fun get(transition: Transition): Arc? {
            return place!!.getArcForTransition(transition)
        }
    }

    inner class WithTransitionGetter {
        var transition: Transition? = null
        operator fun get(place: Place): Arc? {
            return transition!!.getArcForPlace(place)
        }
    }
}
