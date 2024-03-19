package ru.misterpotz.ocgena.simulation.binding.buffer

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.simulation.binding.consumer.OutputTokensBufferConsumerFactory
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import javax.inject.Inject


class TokenGroupCreatorImpl @AssistedInject constructor(
    @Assisted
    override val transition: Transition,
    @Assisted
    private val tokenGroupedInfo: TokenGroupedInfoImpl,
    private val outputTokensBufferConsumerFactory: OutputTokensBufferConsumerFactory
) : TokenGroupCreator, TokenGroupedInfo by tokenGroupedInfo {
    override fun group(tokenBunch: SparseTokenBunch): TokenGroupedInfoImpl {
        val marking = tokenBunch.objectMarking()
        for (place in marking.places) {
            val tokensAtPlace = marking[place]
            tokenGroupedInfo.group(place, tokensAtPlace)
        }
        return tokenGroupedInfo
    }


    override fun createTokensConsumer(): OutputTokensBufferConsumer {
        return outputTokensBufferConsumerFactory.create(
            tokenGroupedInfo,
            transition
        )
    }
}


@AssistedFactory
interface TokenGroupCreatorFactoryAssisted {
    fun create(
        transition: Transition,
        tokenGroupedInfo: TokenGroupedInfoImpl
    ): TokenGroupCreatorImpl
}

class TokenGroupCreatorFactory @Inject constructor(
    private val tokenGroupedInfoFactory: TokenGroupedInfoFactory,
    private val tokenGroupCreatorFactoryAssisted: TokenGroupCreatorFactoryAssisted
) {
    fun create(
        transition: Transition,
    ): TokenGroupCreator {

        val transitionBufferInfoImpl = tokenGroupedInfoFactory.create(transition)

        return tokenGroupCreatorFactoryAssisted.create(transition, transitionBufferInfoImpl)
    }
}
