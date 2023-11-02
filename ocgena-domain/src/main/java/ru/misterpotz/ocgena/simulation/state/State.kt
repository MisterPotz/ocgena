package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.transitions.TransitionInstancesMarking
import ru.misterpotz.marking.transitions.TransitionOccurrenceAllowedTimes
import ru.misterpotz.marking.transitions.TransitionTimesMarking
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet

class State() : SimulatableComposedOcNet.State {
    override val tMarking: TransitionInstancesMarking = TransitionInstancesMarking()
    override val pMarking: ObjectMarking = ObjectMarking()
    override val tTimesMarking: TransitionTimesMarking = TransitionOccurrenceAllowedTimes()
}
