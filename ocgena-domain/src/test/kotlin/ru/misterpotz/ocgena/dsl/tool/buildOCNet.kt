package ru.misterpotz.ocgena.dsl.tool

import org.junit.jupiter.api.Assertions.assertTrue
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastConsistencyDevSetup
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.validation.OCNetChecker

fun buildOCNet(atomDefinitionBlock: OCNetBuilder.AtomDefinitionBlock.() -> Unit): OCNetStruct {
    val ocNet = OCNetBuilder().defineAtoms(atomDefinitionBlock)
    val errors = OCNetChecker(ocNet).checkConsistency()


    assertTrue(
        errors.isEmpty(),
        "ocNet is null, detected errors: ${errors.prettyPrint()}"
    )
    return ocNet
}

fun buildSimplestOCNetNoVar(): OCNetStruct {
    return buildOCNet {
        "p1".p { input }
            .arc("t1".t)
            .arc("p2".p { output })
    }
}

fun defaultSimConfig(
    ocNet: OCNetStruct,
): SimulationConfig {
    return SimulationConfig(
        ocNet,
        initialMarking = MarkingScheme.of {
            put("p1", 10)
        },
        transitionInstancesTimesSpec = TransitionInstancesTimesSpec(
            defaultTransitionTimeSpec = TransitionInstanceTimes(
                duration = Duration(2..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(10..100)
            )
        ),
        randomSeed = 42,
        nodeToLabelRegistry = NodeToLabelRegistry(),
        tokenGeneration = TokenGenerationConfig(
            defaultPeriod = Period(100..120),
            placeIdToGenerationTarget = MarkingScheme.of {
                put("p1", 15)
            }
        ),
        ocNetType = OcNetType.AALST
    )
}

fun domainComponent(): DomainComponent {
    return DomainComponent.create()
}

fun component(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup()
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = DomainComponent.create(),
        developmentDebugConfig = developmentDebugConfig
    )
}

fun simTask(component: SimulationComponent): SimulationTask {
    return component.simulationTask()
}