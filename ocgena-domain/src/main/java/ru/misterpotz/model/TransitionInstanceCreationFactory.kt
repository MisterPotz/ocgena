package model

import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.Time
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