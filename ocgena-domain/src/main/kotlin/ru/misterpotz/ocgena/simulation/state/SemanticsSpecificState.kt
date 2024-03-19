package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType

interface SemanticsSpecificState {
    val type : SimulationSemanticsType
}