package ru.misterpotz.ocgena.simulation_old.state

import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType

interface SemanticsSpecificState {
    val type : SimulationSemanticsType
}