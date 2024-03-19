package ru.misterpotz.ocgena.testing

import io.mockk.every
import io.mockk.mockk
import ru.misterpotz.DBLogger
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation.stepexecutor.SimpleDeterminedTransitionSequenceProvider
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.simulation.stepexecutor.TimePNTransitionMarking
import simulation.random.RandomUseCase
import kotlin.random.Random

fun SimulationComponent.beforeNewStep() {
    simulationStateProvider().onNewStep()
}

interface SimConfigToSimComponentFancyBlock {
    fun setTimeSelectionRandom(times: List<Int>)
    fun setTransitionSelectionRandom(transitions: List<Int>)
    fun setTransitionSelectionSequence(transitions: List<String>)
    fun setTokenSelectionRandom(times: List<Int>)
    fun setDbLogger(dbLogger: DBLogger)
    var dumpStepEndMarking : Boolean
    var dumpTimePnMarking : Boolean
}

private class SimConfigTOSimComponentFancyBlockImpl : SimConfigToSimComponentFancyBlock {
    val mutableMap = mutableMapOf<RandomUseCase, Random>()
    var transitionSequence: List<String> = listOf()
    var _dbLogger : DBLogger? = null
    override fun setTimeSelectionRandom(times: List<Int>) {
        mutableMap[RandomUseCase.TIME_SELECTION] = createPartiallyPredefinedRandSeq(times)
    }

    override fun setTransitionSelectionRandom(transitions: List<Int>) {
        mutableMap[RandomUseCase.TRANSITION_SELECTION] = createPartiallyPredefinedRandSeq(transitions)
    }

    override fun setTransitionSelectionSequence(transitions: List<String>) {
        this.transitionSequence = transitions
    }

    override fun setTokenSelectionRandom(times: List<Int>) {
        mutableMap[RandomUseCase.TOKEN_SELECTION] = createPartiallyPredefinedRandSeq(times)
    }

    override fun setDbLogger(dbLogger: DBLogger) {
        _dbLogger = dbLogger
    }

    override var dumpStepEndMarking: Boolean = false
    override var dumpTimePnMarking: Boolean = false
}

fun SimulationConfig.toSimComponentFancy(
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    block: SimulationConfigToComponentFancyBlockScope
): SimulationComponent {
    val simConfigTOSimComponentFancyBlockImpl = SimConfigTOSimComponentFancyBlockImpl()
    simConfigTOSimComponentFancyBlockImpl.block()
    return simComponent(
        this,
        developmentDebugConfig.copy(
            dumpEndStateMarking = simConfigTOSimComponentFancyBlockImpl.dumpStepEndMarking,
            dumpTimePNTransitionMarking = simConfigTOSimComponentFancyBlockImpl.dumpTimePnMarking
        ),
        randomInstances = simConfigTOSimComponentFancyBlockImpl.mutableMap,
        determinedTransitionSequenceProvider = SimpleDeterminedTransitionSequenceProvider(
            simConfigTOSimComponentFancyBlockImpl.transitionSequence
        ),
        dbLogger = simConfigTOSimComponentFancyBlockImpl._dbLogger,
    )
}

typealias SimulationConfigToComponentFancyBlockScope = SimConfigToSimComponentFancyBlock.() -> Unit

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

fun BatchKey.withTokenBuffer(set: TokenSet): BatchKeyWithBuffer {
    return BatchKeyWithBuffer(this, set)
}


infix fun MutableCollection<BatchKeyWithBuffer>.unaryPlus(batchKey: BatchKeyWithBuffer) {
    add(batchKey)
}

fun ObjectTypeId.withArcMeta(arcMeta: ArcMeta): BatchKey {
    return BatchKey(this.objTypeId(), arcMeta)
}

fun SimulationComponent.emptyTokenBunchBuilder(): SparseTokenBunchImpl.Builder {
    val places = ocNet().placeRegistry.places
    return SparseTokenBunchImpl.makeBuilder {
        for (i in places) {
            forPlace(i.id) {
                type = ocNet().placeToObjectTypeRegistry[i.id]!!
            }
        }
    }
}

fun SimulationComponent.zeroClockedTransitionMarking(): TimePNTransitionMarking {
    val marking = timePNTransitionMarking()
    return marking.copyZeroClock()
}

fun SimulationComponent.facade(): FacadeSim {
    return FacadeSim(
        this,
        task = simulationTask(),
        config = simulationConfig()
    )
}

data class FacadeSim(
    val component: SimulationComponent,
    val task: SimulationTask,
    val config: SimulationConfig,
)

fun SimulationComponent.transition(t: PetriAtomId): Transition {
    val ocNet = ocNet()
    return ocNet.petriAtomRegistry.getTransition(t)
}

fun generateTokensOfType(objectTypeId: ObjectTypeId, vararg objectTokenId: ObjectTokenId): List<ObjectTokenIdAndType> {
    val list = mutableListOf<ObjectTokenIdAndType>()
    for (i in objectTokenId) {
        list.add(
            i.withType(objectTypeId.objTypeId())
        )
    }
    return list
}

fun Long.withType(objectTypeId: ObjectTypeId): ObjectTokenIdAndType {
    return ObjectTokenIdAndType(this, objectTypeId)
}
