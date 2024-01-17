package ru.misterpotz.ocgena.simulation.interactors.timepn

import jdk.jfr.Enabled
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistryImpl
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking

interface EnabledTransitionsResolverTimePN {
    fun getEnabledTransitions(): List<PetriAtomId>
}

class EnabledTransitionsResolverTimePNImpl(
    val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
    val timePNTransitionMarking: TimePNTransitionMarking
) : EnabledTransitionsResolverTimePN {
    override fun getEnabledTransitions(): List<PetriAtomId> {

    }

    fun isTransitionEnabled(petriAtomId: PetriAtomId) : Boolean {
        // TODO: verifying preplace token amount


        return timePNTransitionMarking.getDataForTransition(petriAtomId)
    }
}