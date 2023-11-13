package ru.misterpotz.ocgena.simulation.typea

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OutputArcMultiplicity
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType.*
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsMultiplicityRegistry
import ru.misterpotz.ocgena.registries.ArcsRegistry
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.LockedTokenBufferizer
import ru.misterpotz.ocgena.simulation.binding.LockedTokenMoveStrategy
import ru.misterpotz.ocgena.simulation.binding.OutputTokensProducer
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import java.util.*
import javax.inject.Inject

typealias TokenBuffer = SortedSet<ObjectTokenId>


class TokenBatch(
    val sortedSet: SortedSet<ObjectTokenId>,
    val objectTypeId: ObjectTypeId,
    val arcMeta: ArcMeta,
)

class TokenBatchListFactory @Inject constructor(
    private val ocNet: OCNet,
) {
    fun createFor(transition: PetriAtomId): TokenBatchList {
        return TokenBatchList(
            placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
            petriAtomRegistry = ocNet.petriAtomRegistry,
            transition = transition
        )
    }
}

interface BufferBatchalizationStrategy {
    // group by arc variable name, or have different batches for different variable names?
    fun findExistingBatch(
        tokenBatchs: List<TokenBatch>,
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ) : TokenBatch?

    // when selecting tokens for place, treat tokens coming from arcs of different variable names
    // as relative?
    //
    fun findSourceTokenBatchForPlace(place : PetriAtomId) : TokenBatch
}

class TokenBatchList(
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val petriAtomRegistry: PetriAtomRegistry,
    private val transition: PetriAtomId,
    private val arcsRegistry: ArcsRegistry,
    private val batchalizationStrategy: BufferBatchalizationStrategy
) {
    private val tokenBatches: MutableList<TokenBatch> = mutableListOf()
    fun addTokens(
        place: PetriAtomId,
        sortedSet: SortedSet<ObjectTokenId>
    ) {
        val objectType = placeToObjectTypeRegistry[place]
        val arcId = place.arcIdTo(transition)
        val arc = arcsRegistry[arcId]

        val similarTokenBatch = batchalizationStrategy.findExistingBatch(
            tokenBatches,
            objectType,
            arc.arcMeta
        )

        @Suppress("IfThenToElvis")
        if (similarTokenBatch != null) {
            similarTokenBatch.sortedSet.addAll(sortedSet)
        } else {
            tokenBatches.add(TokenBatch(sortedSet, objectType, arc.arcMeta))
        }
    }

    fun getBatchBy(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): SortedSet<ObjectTokenId> {
        return tokenBatches.find {
            it.objectTypeId == objectTypeId
        }!!.sortedSet
    }
}

class CommonBufferizerFactory @Inject constructor(
    val ocNet: OCNet,
    val tokenBatchListFactory: TokenBatchListFactory
) {
    fun create(transition: Transition): CommonBufferizer {
        return CommonBufferizer(
            transition = transition,
            tokenBatchListFactory = tokenBatchListFactory,
            petriAtomRegistry = ocNet.petriAtomRegistry
        )
    }
}

interface TransitionBufferInfo {
    fun getBatchBy(
        objectTypeId: ObjectTypeId,
        arcMeta: ArcMeta
    ): TokenBuffer

    fun getInputArcs(): Collection<Arc>

    fun getItemsPerArc(arc: Arc): Int
}

class CommonBufferizer(
    private val transition: Transition,
    private val petriAtomRegistry: PetriAtomRegistry,
    tokenBatchListFactory: TokenBatchListFactory,
) : TransitionBufferInfo {
    private val tokenBatchList: TokenBatchList = tokenBatchListFactory.createFor(transition.id)
    private val arcPerBatchSize = mutableMapOf<Arc, Int>()
    private val bufferizedPlaces = mutableSetOf<PetriAtomId>()

    fun bufferize(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>) {
        if (bufferizedPlaces.contains(place)) return
        bufferizedPlaces.add(place)

        val arc = petriAtomRegistry.getArc(place.arcIdTo(transition.id))
        val batchSize = tokens.size
        arcPerBatchSize[arc] = batchSize

        tokenBatchList.addTokens(place, tokens)
    }

    override fun getBatchBy(objectTypeId: ObjectTypeId): SortedSet<ObjectTokenId> {
        return tokenBatchList.getBatchBy(objectTypeId)
    }

    override fun getInputArcs(): Collection<Arc> {
        return arcPerBatchSize.keys
    }

    override fun getItemsPerArc(arc: Arc): Int {
        return arcPerBatchSize[arc]!!
    }

    fun getBufferizedArcs(): Collection<Arc> {
        return arcPerBatchSize.keys
    }

    fun getBatchSizePerArc(arc: Arc): Int {
        return arcPerBatchSize[arc]!!
    }
}

class ProducedMarkingBuffer(
    val outputMarking: PlaceToObjectMarking = PlaceToObjectMarking()
)

interface OutputTokensProducerFactory {
    fun create(commonBufferizer: CommonBufferizer): OutputTokensProducer
}

class CommonBufferizerWrapper(
    private val commonBufferizer: CommonBufferizer,
    private val outputTokensProducerFactory: OutputTokensProducerFactory
) : LockedTokenBufferizer {
    override fun bufferize(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>) {
        commonBufferizer.bufferize(place, tokens)
    }

    override fun createProducer(): OutputTokensProducer {
        return outputTokensProducerFactory.create(commonBufferizer)
    }
}


class LockedTokenMoveStrategyTypeA(
    private val transition: Transition,
    private val commonBufferizer: CommonBufferizerFactory,
    private val ocNet: OCNet,
    private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor
) : LockedTokenMoveStrategy {
    class OutputTokensProducerFactoryTypeA(
        private val ocNet: OCNet,
        private val transition: Transition,
        private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
        private val tokenSelectionInteractor: TokenSelectionInteractor
    ) :
        OutputTokensProducerFactory {
        override fun create(
            commonBufferizer: CommonBufferizer,
        ): OutputTokensProducer {
            return OutputTokensProducerTypeA(
                transition = transition,
                commonBufferizer = commonBufferizer,
                petriAtomRegistry = ocNet.petriAtomRegistry,
                placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
                arcsMultiplicityRegistry = arcsMultiplicityRegistry,
                tokenSelectionInteractor = tokenSelectionInteractor
            )
        }
    }

    interface BufferConsumptionStrategy {
        //
        fun getConsumedTokensForPlace(place: PetriAtomId): TokenBuffer
    }

    class OutputTokensProducerTypeA(
        private val transition: Transition,
        private val commonBufferizer: CommonBufferizer,
        private val petriAtomRegistry: PetriAtomRegistry,
        private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
        private val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
        private val tokenSelectionInteractor: TokenSelectionInteractor,
        private val bufferConsumptionStrategy: BufferConsumptionStrategy
    ) : OutputTokensProducer {

        private val producedMarkingBuffer = ProducedMarkingBuffer()
        private val placeToArcMultiplicity: MutableMap<PetriAtomId, OutputArcMultiplicity> = mutableMapOf()

        override fun produceByConsumeTokenBuffer(place: PetriAtomId) {
            return with(petriAtomRegistry) {
                val objectTypeId = placeToObjectTypeRegistry[place]
                val arcType = transition.id.arcTo(place).arcType

                val arcMultiplicity = placeToArcMultiplicity.getOrPut(place) {
                    arcsMultiplicityRegistry.transitionOutputMultiplicity(
                        commonBufferizer,
                        transition.id.arcIdTo(place)
                    )
                }

                val tokenBuffer = arcMultiplicity.getTokenSourceForThisArc()
                val requiredTokenAmount = arcMultiplicity.requiredTokenAmount()


                tokenSelectionInteractor.selectAndGenerateTokensFromBuffer(relevantBatch)
            }
        }

        override fun produceByNewTokenGeneration(place: PetriAtomId) {
            TODO("Not yet implemented")
        }

        override fun finalTokenBufferForPlace(place: PetriAtomId): TokenBuffer {
            TODO("Not yet implemented")
        }
    }


    override fun createBufferizer(): LockedTokenBufferizer {
        val commonBufferizer = commonBufferizer.create(transition)
        return CommonBufferizerWrapper(
            commonBufferizer,
            outputTokensProducerFactory = OutputTokensProducerFactoryTypeA(
                ocNet = ocNet,
                transition = transition,
                arcsMultiplicityRegistry = arcsMultiplicityRegistry,
                tokenSelectionInteractor = tokenSelectionInteractor
            )
        )
    }
}

fun kek() {
    val outputMarking = PlaceToObjectMarking()

    return with(petriAtomRegistry) {
        val outputPlaces = getTransition(transitionInstance.transition).toPlaces

        for (outputPlace in repeatabilityInteractor.sortPlaces(outputPlaces)) {
            val arc = transitionInstance.transition.arcTo(outputPlace)
            val type = placeToObjectTypeRegistry[outputPlace]

            val consumedTokens = when (arc) {
                is NormalArc -> {
                    val tokenStack = normalTokens[type]
                    tokenStack?.tryConsume(arc.multiplicity)
                }

                is VariableArc -> {
                    val tokenStack = variableTokens[type]
                    tokenStack?.tryConsumeAll()
                }

                else -> throw IllegalStateException("not supportah this ark typah: $arc")
            }
            outputMarking[outputPlace] = consumedTokens?.toSortedSet() ?: sortedSetOf()
        }
        outputMarking
    }

}
