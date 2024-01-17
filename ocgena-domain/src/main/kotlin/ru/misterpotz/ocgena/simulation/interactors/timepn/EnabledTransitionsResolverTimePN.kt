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
        return listOf()
    }

    fun isTransitionEnabled(petriAtomId: PetriAtomId) : Boolean {
        // TODO: verifying preplace token amount
        return true
//        return timePNTransitionMarking.getDataForTransition(petriAtomId)
    }
}