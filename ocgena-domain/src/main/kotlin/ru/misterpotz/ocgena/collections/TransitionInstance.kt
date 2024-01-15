package ru.misterpotz.ocgena.collections

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.ocnet.primitives.atoms.TransitionId
import ru.misterpotz.ocgena.utils.ANSI_CYAN
import ru.misterpotz.ocgena.utils.ANSI_YELLOW

typealias TI = TransitionInstance

data class TransitionInstance(
    val transition: TransitionId,
    val relativeTimePassedSinceLock: Time,
    val duration: Time,
    val startedAt: Time,
    val tokenSynchronizationTime: Time,
    val lockedObjectTokens: ImmutablePlaceToObjectMarking,
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
