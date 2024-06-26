package ru.misterpotz.ocgena.simulation_old.interactors

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.simulation_old.binding.buffer.TokenGroupCreatorFactory
import ru.misterpotz.ocgena.simulation_old.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation_old.stepexecutor.toImmutableBunch
import javax.inject.Inject


class TIOutputPlacesResolverInteractorImpl @Inject constructor(
    private val tokenGroupCreatorFactory: TokenGroupCreatorFactory,
    ocNet: OCNet,
) : TIOutputPlacesResolverInteractor {
    private val petriAtomRegistry = ocNet.petriAtomRegistry

    override fun createOutputMarking(activeFiringTransition: TransitionInstance): ImmutablePlaceToObjectMarking {
        val lockedTokens = activeFiringTransition.lockedObjectTokens
        val transition = petriAtomRegistry.getTransition(activeFiringTransition.transition)

        val lockedTokensBufferizer = tokenGroupCreatorFactory.create(transition = transition)

        lockedTokensBufferizer.group(lockedTokens.toImmutableBunch())

        val tokenConsumer = lockedTokensBufferizer.createTokensConsumer()

        val tokenGenerator = tokenConsumer.consumeTokenBuffer()

        val finalOutputMarking = tokenGenerator.generateMissingTokens()

        return finalOutputMarking
    }
}
