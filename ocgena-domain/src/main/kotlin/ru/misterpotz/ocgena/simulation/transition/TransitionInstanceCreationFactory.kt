package ru.misterpotz.ocgena.simulation.transition

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.ocnet.primitives.atoms.TransitionId
import ru.misterpotz.ocgena.simulation.Time
import javax.inject.Inject

class TransitionInstanceCreationFactory @Inject constructor() {
    fun create(
        transition: TransitionId,
        lockedObjectTokens: ImmutablePlaceToObjectMarking,
        duration: Time,
        startedAt: Time,
        tokenSynchronizationTime: Time,
    ): TransitionInstance {
        require(lockedObjectTokens.isEmpty().not())

        return TransitionInstance(
            transition = transition,
            relativeTimePassedSinceLock = 0,
            lockedObjectTokens = lockedObjectTokens,
            duration = duration,
            startedAt = startedAt,
            tokenSynchronizationTime = tokenSynchronizationTime
        )
    }
}