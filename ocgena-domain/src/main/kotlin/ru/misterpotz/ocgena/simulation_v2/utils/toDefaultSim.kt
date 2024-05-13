package ru.misterpotz.ocgena.simulation_v2.utils

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput

fun OCNetStruct.toDefaultSim(simulationInput: SimulationInput = SimulationInput()) =
    ModelAccessor(this, simulationInput).apply {
        init()
    }

class Ref<T>() {
    var _ref: T? = null

    val ref: T
        get() = _ref!!
}

interface Identifiable {
    val id: String
}

fun <T : Identifiable> List<T>.selectIn(iterable: Iterable<String>) = filter { it.id in iterable }

