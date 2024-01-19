package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.binding.consumer.OutputTokensBufferConsumerFactory
import javax.inject.Inject

class TokenGroupCreatorImpl(
    private val transitionBufferInfoImpl: TransitionGroupedTokenInfoImpl,
    override val transition: Transition,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) : TokenGroupCreator, TransitionGroupedTokenInfo by transitionBufferInfoImpl {
    override fun group(fromPlacesMarking: ImmutablePlaceToObjectMarking) {
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

class TokenGroupCreatorFactory @Inject constructor(
    private val ocNet: OCNet,
    private val tokenBatchListFactory: TokenBatchListFactory,
    private val tokenGroupingStrategy: TransitionGroupedTokenInfo.TokenGroupingStrategy,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) {
    private val petriAtomRegistry: PetriAtomRegistry = ocNet.petriAtomRegistry
    fun create(
        transition: Transition,
    ): TokenGroupCreator {

        val transitionBufferInfoImpl = TransitionGroupedTokenInfoImpl(
            transition = transition,
            petriAtomRegistry = petriAtomRegistry,
            tokenBatchListFactory = tokenBatchListFactory,
            ocNet = ocNet,
            tokenGroupingStrategy = tokenGroupingStrategy
        )

        return TokenGroupCreatorImpl(
            transitionBufferInfoImpl = transitionBufferInfoImpl,
            transition = transition,
            outputTokensBufferConsumerFactory = outputTokensBufferConsumerFactory
        )
    }
}
