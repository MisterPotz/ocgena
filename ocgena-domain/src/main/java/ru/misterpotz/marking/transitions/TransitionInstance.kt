package ru.misterpotz.marking.transitions

import kotlinx.serialization.Serializable
import model.TransitionId
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.Time
import utils.ANSI_CYAN
import utils.ANSI_YELLOW

@Serializable
data class TransitionInstance(
    val transition: TransitionId,
    val relativeTimePassedSinceLock: Time,
    val duration: Time,
    val startedAt: Time,
    val tokenSynchronizationTime: Time,
    val lockedObjectTokens: ImmutableObjectMarking,
) {
    fun timeLeftUntilFinish(): Time {
        return (duration - relativeTimePassedSinceLock).coerceAtLeast(0)
    }

    override fun toString(): String {
        return """$ANSI_CYAN> started $transition : 
            |   $ANSI_YELLOW[locked]:
            |$ANSI_YELLOW${lockedObjectTokens.toString().prependIndent("\t")}
        """.trimMargin()
    }
}
