package ru.misterpotz.ocgena.testing

import io.mockk.every
import io.mockk.mockk
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation_old.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.simulation_old.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation_old.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType
import kotlin.random.Random


interface ConfigBuilderBlock {
    var ocNetStruct : OCNetStruct?
    var semanticsType: SimulationSemanticsType?
    var ocNetType: OcNetType?
    var timePnSpec : TransitionsTimePNSpec?
}

private class ConfigBuilderBlockImpl : ConfigBuilderBlock {
    override var ocNetStruct: OCNetStruct? = null
    override var semanticsType: SimulationSemanticsType? = null
    override var ocNetType: OcNetType? = null
    override var timePnSpec: TransitionsTimePNSpec? = null
}

typealias ConfigBuilderBlockScope = ConfigBuilderBlock.() -> Unit

fun buildConfig(configBuilderBlock: ConfigBuilderBlockScope): SimulationConfig {
    val receiver = ConfigBuilderBlockImpl()
    receiver.configBuilderBlock()
    return SimulationConfig(
        ocNet = receiver.ocNetStruct!!,
        transitionsSpec = when (receiver.semanticsType!!) {
            SimulationSemanticsType.ORIGINAL -> {
                TransitionsOriginalSpec()
            }

            SimulationSemanticsType.SIMPLE_TIME_PN -> {
                receiver.timePnSpec ?: TransitionsTimePNSpec()
            }
        },
        initialMarking = MarkingScheme.of { },
        randomSeed = null,
        tokenGeneration = null,
        ocNetType = receiver.ocNetType!!,
        simulationSemantics = SimulationSemantics(receiver.semanticsType!!)
    )
}

fun createPartiallyPredefinedRandSeq(
    seq: List<Int>,
    fallback: () -> Int = { throw IllegalStateException("unexpectedly reached test fallback") }
): Random {
    val mSeq = seq.toMutableList()
    return mockk<Random> {
        every { nextInt(any(), any()) } answers {
            mSeq.removeFirstOrNull() ?: fallback()
        }
        every { nextLong(any(), any()) } answers {
            mSeq.removeFirstOrNull()?.toLong() ?: fallback().toLong()
        }
        every {
            nextInt(any())
        } answers {
            mSeq.removeFirstOrNull() ?: fallback()
        }
    }
}

fun SimulationConfig.toSimComponent(
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstance: Random? = null
): SimulationComponent {
    return simComponentOld(this, developmentDebugConfig, randomInstance = randomInstance)
}

