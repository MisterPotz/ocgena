package ru.misterpotz.ocgena.simulation_v2.utils

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun OCNetStruct.toDefaultSim(simulationInput: SimulationInput = SimulationInput()) =
    ModelAccessor(this, simulationInput).apply {
        init()
    }

class Ref<T> {
    private var _ref: T? = null

    fun setRef(value : T?) {
        _ref = value
    }

    override fun toString(): String {
        return "Ref($_ref)"
    }

    val nullable: T?
        get() = _ref
    val ref: T
        get() = _ref!!


}

interface Identifiable {
    val id: String
}

fun <T : Identifiable> List<T>.selectIn(iterable: Iterable<String>) = filter { it.id in iterable }

@OptIn(ExperimentalContracts::class)
suspend inline fun <T> sstep(description: String, crossinline action: suspend () -> T): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return run {
        action()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> step(description: String, crossinline action: () -> T): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return action()
}

fun Int.factorial(): Int {
    if (this == 0) return 1
    if (this == 1) return 1

    return this * (this - 1).factorial()
}
