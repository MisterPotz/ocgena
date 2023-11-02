package ru.misterpotz.ocgena.collections.transitions

import kotlinx.serialization.Serializable
import model.ArcsRegistry
import model.TransitionsRegistry
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.TransitionId
import utils.ANSI_CYAN
import utils.ANSI_YELLOW

typealias TI = TransitionInstance

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
