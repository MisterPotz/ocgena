package ru.misterpotz.ocgena.dsl.tool

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertTrue
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.dsl.simulation.TestFolder
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.utils.findInstance
import ru.misterpotz.ocgena.validation.OCNetChecker
import java.io.File
import java.lang.IllegalStateException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString

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

class FacadeSimulation(
    val simulationComponent: SimulationComponent,
    val simulationTask: SimulationTask,
)

//fun facadeCreateSimulation(
//    simulationConfig: SimulationConfig,
//    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup()
//
//): FacadeSimulation {
//    return
//}

val resPath = "src/test/resources/"
val res = File(resPath)

fun config(name: String): File {
    return File(res, name)
}

fun config(path: Path): File {
    return File(path.pathString)
}

fun String.writeConfig(path: Path) {
    val pathCorrected = appendPathWithRes(path = path)
    pathCorrected.toFile().writeText(this)
}

val comp = domainComponent()
inline fun <reified T> jsonConfig(name: String): T {
    val text = config(name).readText()

    return comp.json.decodeFromString<T>(text)
}

inline fun <reified T> yamlConfig(name: String): T {
    val text = config(name).readText()

    return comp.yaml.decodeFromString<T>(text)
}

inline fun <reified T> jsonConfig(path: Path): T {

    val text = config(path).readText()

    return comp.json.decodeFromString<T>(text)
}

inline fun <reified T> yamlConfig(path: Path): T {
    val text = config(path).readText()

    return comp.yaml.decodeFromString<T>(text)
}

inline fun <reified T> T.toYaml(): String {
    return comp.yaml.encodeToString(this)
}

inline fun <reified T> T.toJson(): String {
    return comp.json.encodeToString(this)
}

inline fun <reified T> readConfig(name: String): T {
    return if (name.endsWith(".json")) {
        jsonConfig<T>(name)
    } else if (name.endsWith(".yaml")) {
        yamlConfig<T>(name)
    } else {
        throw IllegalStateException()
    }
}

fun appendPathWithRes(path: Path): Path {
    val resPath = Path(resPath)
    return if (path.first() == resPath) {
        path
    } else {
        resPath / path
    }
}

inline fun <reified T> readConfig(path: Path): T {
    val path = appendPathWithRes(path = path)

    return if (path.pathString.endsWith(".json")) {
        jsonConfig<T>(path)
    } else if (path.pathString.endsWith(".yaml")) {
        yamlConfig<T>(path)
    } else {
        throw IllegalStateException()
    }
}

inline fun <reified T> readFolderConfig(folderName: String, name: String): T {
    val withFolderPath = Path(resPath) / folderName / name

    val string = withFolderPath.pathString
    return if (string.endsWith(".json")) {
        jsonConfig<T>(withFolderPath)
    } else if (string.endsWith(".yaml")) {
        yamlConfig<T>(withFolderPath)
    } else {
        throw IllegalStateException()
    }
}

inline fun <reified T, R : Any> T.withFolderName(action: (String) -> R): R {
    val folderName = this!!::class.annotations.findInstance<TestFolder>()!!.folderName
    return action(folderName)
}

inline fun <reified T, reified R : Any> T.readFolderConfig(name: String): R {
    return withFolderName {
        readFolderConfig<R>(it, name)
    }
}
