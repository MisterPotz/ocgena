package ru.misterpotz.ocgena.testing

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.di.DomainComponentDependencies
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.ocnet.utils.makeObjTypeId
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.config.original.Duration
import ru.misterpotz.ocgena.simulation.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.validation.OCNetChecker
import simulation.random.RandomUseCase
import java.util.*
import kotlin.random.Random

val DOMAIN_COMPONENT = domainComponent()
val USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME = true

typealias OCNetBuildingCodeBlock = OCNetBuilder.AtomDefinitionBlock.() -> Unit

fun OCNetBuilder.AtomDefinitionBlock.install(oCNetBuildingCodeBlock: OCNetBuildingCodeBlock) {
    oCNetBuildingCodeBlock.invoke(this)
}

fun OCNetBuildingCodeBlock.installOnto(builder: OCNetBuilder.AtomDefinitionBlock) {
    builder.install(this)
}

fun buildOCNet(atomDefinitionBlock: OCNetBuildingCodeBlock): OCNetStruct {
    val ocNet = OCNetBuilder().defineAtoms(atomDefinitionBlock)
    val errors = OCNetChecker(ocNet).checkConsistency()


    assert(
        errors.isEmpty()
    ) {
        "ocNet is null, detected errors: ${errors.prettyPrint()}"
    }

    return ocNet
}

fun buildSimplestOCNetNoVar(): OCNetStruct {
    return buildOCNet {
        "p1".p { input }
            .arc("t1".t)
            .arc("p2".p { output })
    }
}

fun defaultSimConfigOriginal(
    ocNet: OCNetStruct,
): SimulationConfig {
    return SimulationConfig(
        ocNet,
        initialMarking = MarkingScheme.of {
            put("p1", 10)
        },
        transitionsSpec = TransitionsOriginalSpec(
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
        ocNetType = OcNetType.AALST,
        simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.ORIGINAL)
    )
}

fun defaultSimConfigTimePN(
    ocNet: OCNetStruct,
): SimulationConfig {
    return SimulationConfig(
        ocNet,
        initialMarking = MarkingScheme.of {
            put("p1", 10)
        },
        transitionsSpec = TransitionsOriginalSpec(
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
        ocNetType = OcNetType.AALST,
        simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.SIMPLE_TIME_PN)
    )
}


fun domainComponent(): DomainComponent {
    val domainComponentDependenciesMockk = mockk<DomainComponentDependencies> {
        every { dbLogger() } returns mockk(relaxed = true, relaxUnitFun = true)
    }
    return DomainComponent.create(domainComponentDependenciesMockk)
}

fun simComponent(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstances: Map<RandomUseCase, Random> = mutableMapOf()
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = DOMAIN_COMPONENT,
        randomInstances = randomInstances,
        developmentDebugConfig = developmentDebugConfig
    )
}

fun SimulationComponent.beforeNewStep() {
    simulationStateProvider().onNewStep()
}

fun simComponentOld(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstance: Random? = null
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = DOMAIN_COMPONENT,
        randomInstances = listOf(RandomUseCase.TIME_SELECTION to randomInstance).filter {
            it.second != null
        }.associateBy { it.first }
            .mapValues { it.value.second!! },
        developmentDebugConfig = developmentDebugConfig
    )
}

fun simTask(component: SimulationComponent): SimulationTask {
    return component.simulationTask()
}

interface SimConfigToSimComponentFancyBlock {
    fun setTimeSelectionRandom(times: List<Int>)
    fun setTransitionSelectionRandom(transitions: List<Int>)
    fun setTokenSelectionRandom(times: List<Int>)
}

private class SimConfigTOSimComponentFancyBlockImpl : SimConfigToSimComponentFancyBlock {
    val mutableMap = mutableMapOf<RandomUseCase, Random>()

    override fun setTimeSelectionRandom(times: List<Int>) {
        mutableMap[RandomUseCase.TIME_SELECTION] = createPartiallyPredefinedRandSeq(times)
    }

    override fun setTransitionSelectionRandom(transitions: List<Int>) {
        mutableMap[RandomUseCase.TRANSITION_SELECTION] = createPartiallyPredefinedRandSeq(transitions)
    }

    override fun setTokenSelectionRandom(times: List<Int>) {
        mutableMap[RandomUseCase.TOKEN_SELECTION] = createPartiallyPredefinedRandSeq(times)
    }
}

fun SimulationConfig.toSimComponentFancy(
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    block: SimulationConfigToComponentFancyBlockScope
): SimulationComponent {
    val simConfigTOSimComponentFancyBlockImpl = SimConfigTOSimComponentFancyBlockImpl()
    simConfigTOSimComponentFancyBlockImpl.block()
    return simComponent(
        this,
        developmentDebugConfig,
        randomInstances = simConfigTOSimComponentFancyBlockImpl.mutableMap
    )
}

typealias SimulationConfigToComponentFancyBlockScope = SimConfigToSimComponentFancyBlock.() -> Unit

fun SimulationConfig.toSimComponent(
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstance: Random? = null
): SimulationComponent {
    return simComponentOld(this, developmentDebugConfig, randomInstance = randomInstance)
}

fun SimulationComponent.facade(): FacadeSim {
    return FacadeSim(
        this,
        task = simulationTask(),
        config = simulationConfig()
    )
}


data class ObjectTokenIdAndType(val objectTokenId: ObjectTokenId, val objectTypeId: ObjectTypeId)

fun generateTokensOfType(objectTypeId: ObjectTypeId, vararg objectTokenId: ObjectTokenId): List<ObjectTokenIdAndType> {
    val list = mutableListOf<ObjectTokenIdAndType>()
    for (i in objectTokenId) {
        list.add(
            i.withType(objectTypeId.objTypeId())
        )
    }
    return list
}

fun ImmutablePlaceToObjectMarking.loggableString(simulationComponent: SimulationComponent): String {
    this as ImmutablePlaceToObjectMarkingMap

    val loggable = LoggableImmutablePlaceToObjectMarkingMap(
        placesToObjectTokens.mapValues {
            it.value.map {
                simulationComponent.objectTokenSet()[it]?.name!!
            }.toSortedSet()
        }
    )
    return loggable.placesToObjectTokens.toString()
}

@Serializable
@SerialName("placeToObject")
private data class LoggableImmutablePlaceToObjectMarkingMap(
    @SerialName("per_place") val placesToObjectTokens: Map<PetriAtomId, SortedSet<String>>,
)

fun Long.withType(objectTypeId: ObjectTypeId): ObjectTokenIdAndType {
    return ObjectTokenIdAndType(this, objectTypeId)
}

fun SimulationComponent.withGenerateTokens(ids: MutableList<ObjectTokenIdAndType>.() -> Unit): SimulationComponent {
    val objectTokenSet = objectTokenSet()
    val generator = objectTokenGenerator()
    val mutableList = mutableListOf<ObjectTokenIdAndType>()
    mutableList.ids()
    val ocNet = ocNet()

    for (i in mutableList) {
        objectTokenSet.add(
            generator.generate(
                type = ocNet.objectTypeRegistry[i.objectTypeId],
                id = i.objectTokenId
            )
        )
    }
    return this
}

fun SimulationComponent.addTokens(block: AddTokensBlock.() -> Unit): SimulationComponent {
    val tokenBunch = tokenBunch()
    val receiver = AddTokensBlockImpl(this)
    receiver.block()
    for ((place, amount) in receiver.placeToAmount) {
        tokenBunch.tokenAmountStorage().applyDeltaTo(place, +amount)
    }
    return this
}

fun SimulationComponent.addBunch(sparseTokenBunch: SparseTokenBunch): SimulationComponent {
    val globalTokenBunch = tokenBunch()
    globalTokenBunch.append(sparseTokenBunch)
    return this
}

interface AddTokensBlock {
    fun forPlace(place: PetriAtomId, amount: Int)
}

private class AddTokensBlockImpl(private val simulationComponent: SimulationComponent) : AddTokensBlock {
    val placeToAmount: MutableMap<PetriAtomId, Int> = mutableMapOf()

    override fun forPlace(place: PetriAtomId, amount: Int) {
        val amount = placeToAmount.getOrPut(place) { 0 } + amount
        placeToAmount.put(place, amount)
    }
}

data class FacadeSim(
    val component: SimulationComponent,
    val task: SimulationTask,
    val config: SimulationConfig,
)

//fun facadeCreateSimulation(
//    simulationConfig: SimulationConfig,
//    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup()
//
//): FacadeSimulation {
//    return
//}


fun SimulationComponent.transition(t: PetriAtomId): Transition {
    val ocNet = ocNet()
    return ocNet.petriAtomRegistry.getTransition(t)
}

fun SimulationComponent.emptyTokenBunchBuilder(): SparseTokenBunchImpl.Builder {
    val places = ocNet().placeRegistry.places
    return SparseTokenBunchImpl.makeBuilder {
        for (i in places) {
            forPlace(i.id) {

            }
        }
    }
}

fun SimulationComponent.mockTransitionBufferInfo(
    t: PetriAtomId,
    block: MutableCollection<BatchKeyWithBuffer>.() -> Unit,
): TokenGroupedInfo {

    val trans = transition(t)
    val simBatchGroupingStrategy = batchGroupingStrategy()

    return mockk<TokenGroupedInfo> {
        every { transition } returns trans
        every { tokenGroupingStrategy } returns simBatchGroupingStrategy

        val mutableCollection = mutableListOf<BatchKeyWithBuffer>()
        mutableCollection.block()

        for (i in mutableCollection) {
            every {
                getTokenSetBy(
                    i.batchKey.objectTypeId,
                    i.batchKey.arcMeta,
                )
            } returns i.tokenSet
        }
    }
}


data class BatchKey(val objectTypeId: ObjectTypeId, val arcMeta: ArcMeta)
data class BatchKeyWithBuffer(val batchKey: BatchKey, val tokenSet: TokenSet)

fun ObjectTypeId.withArcMeta(arcMeta: ArcMeta): BatchKey {
    return BatchKey(this.objTypeId(), arcMeta)
}

fun String.objTypeId(): ObjectTypeId {
    return if (USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME) {
        makeObjTypeId()
    } else {
        this
    }
}

fun BatchKey.withTokenBuffer(set: TokenSet): BatchKeyWithBuffer {
    return BatchKeyWithBuffer(this, set)
}


infix fun MutableCollection<BatchKeyWithBuffer>.unaryPlus(batchKey: BatchKeyWithBuffer) {
    add(batchKey)
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


interface ConfigBuilderBlock {
    var ocNetStruct : OCNetStruct?
    var semanticsType: SimulationSemanticsType?
    var ocNetType: OcNetType?
    var timePnSpec : TransitionsTimePNSpec?
}

private class ConfigBuilderBlockImpl() : ConfigBuilderBlock {
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
