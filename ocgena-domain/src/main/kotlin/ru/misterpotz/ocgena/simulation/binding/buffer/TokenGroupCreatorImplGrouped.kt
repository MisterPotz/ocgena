package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.binding.consumer.OutputTokensBufferConsumerFactory
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import javax.inject.Inject

class TokenGroupCreatorImplGrouped(
    private val tokenGroupedInfo: TokenGroupedInfoImpl,
    override val transition: Transition,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) : TokenGroupCreator, TokenGroupedInfo by tokenGroupedInfo {
    override fun group(tokenBunch: SparseTokenBunch) {
        val marking = tokenBunch.objectMarking()
        for (place in marking.places) {
            val tokensAtPlace = marking[place]
            tokenGroupedInfo.group(place, tokensAtPlace)
        }
    }

    override fun getGroupedInfo(): TokenGroupedInfo {
        return tokenGroupedInfo
    }

    override fun createTokensConsumer(): OutputTokensBufferConsumer {
        return outputTokensBufferConsumerFactory.create(
            tokenGroupedInfo,
            transition
        )
    }
}

class TokenGroupCreatorFactory @Inject constructor(
    private val ocNet: OCNet,
    private val tokenBatchListFactory: TokenBatchListFactory,
    private val tokenGroupingStrategy: TokenGroupedInfo.TokenGroupingStrategy,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) {
    private val petriAtomRegistry: PetriAtomRegistry = ocNet.petriAtomRegistry
    fun create(
        transition: Transition,
    ): TokenGroupCreator {

        val transitionBufferInfoImpl = TokenGroupedInfoImpl(
            transition = transition,
            tokenBatchListFactory = tokenBatchListFactory,
            ocNet = ocNet,
            tokenGroupingStrategy = tokenGroupingStrategy
        )

        return TokenGroupCreatorImplGrouped(
            tokenGroupedInfo = transitionBufferInfoImpl,
            transition = transition,
            outputTokensBufferConsumerFactory = outputTokensBufferConsumerFactory
        )
    }
}
