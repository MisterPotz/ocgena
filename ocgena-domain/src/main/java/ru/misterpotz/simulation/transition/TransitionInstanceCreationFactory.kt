package ru.misterpotz.simulation.transition

import ru.misterpotz.model.atoms.TransitionId
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.Time
import ru.misterpotz.marking.transitions.TransitionInstance
import javax.inject.Inject

class TransitionInstanceCreationFactory @Inject constructor() {
    fun create(
        transition: TransitionId,
        lockedObjectTokens: ImmutableObjectMarking,
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