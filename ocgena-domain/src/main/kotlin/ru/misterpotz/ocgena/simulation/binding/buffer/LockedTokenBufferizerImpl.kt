package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.binding.consumer.OutputTokensBufferConsumerFactory
import javax.inject.Inject

class LockedTokenBufferizerImpl(
    private val transitionBufferInfoImpl: TransitionBufferInfoImpl,
    override val transition: Transition,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) : LockedTokenBufferizer, TransitionBufferInfo by transitionBufferInfoImpl {
    override fun bufferize(fromPlacesMarking: ImmutablePlaceToObjectMarking) {
        for (place in fromPlacesMarking.keys) {
            val tokensAtPlace = fromPlacesMarking[place]
            transitionBufferInfoImpl.bufferize(place, tokensAtPlace)
        }
    }

    override fun createTokensConsumer(): OutputTokensBufferConsumer {
        return outputTokensBufferConsumerFactory.create(
            transitionBufferInfoImpl,
            transition
        )
    }
}

class LockedTokensBufferizerFactory @Inject constructor(
    private val ocNet: OCNet,
    private val tokenBatchListFactory: TokenBatchListFactory,
    private val batchGroupingStrategy: TransitionBufferInfo.BatchGroupingStrategy,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) {
    private val petriAtomRegistry: PetriAtomRegistry = ocNet.petriAtomRegistry
    fun create(
        transition: Transition,
    ): LockedTokenBufferizer {

        val transitionBufferInfoImpl = TransitionBufferInfoImpl(
            transition = transition,
            petriAtomRegistry = petriAtomRegistry,
            tokenBatchListFactory = tokenBatchListFactory,
            ocNet = ocNet,
            batchGroupingStrategy = batchGroupingStrategy
        )

        return LockedTokenBufferizerImpl(
            transitionBufferInfoImpl = transitionBufferInfoImpl,
            transition = transition,
            outputTokensBufferConsumerFactory = outputTokensBufferConsumerFactory
        )
    }
}
