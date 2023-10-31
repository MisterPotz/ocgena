package model

import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.Time
import utils.*

data class ExecutedBinding(
    val finishedActivity: TransitionInstance,
    val finishedTime: Time,
    val consumedMap: ImmutableObjectMarking,
    val producedMap: ImmutableObjectMarking,
) {

    override fun toString(): String {
        return """$ANSI_PURPLE> executed ${finishedActivity.transition}
            |${"\t$ANSI_RED"}- consumed:
            |${ANSI_RED}${consumedMap.toString().prependIndent("\t- ")}
            |$ANSI_GREEN${"\t"}+ produced:
            |$ANSI_GREEN${producedMap.toString().prependIndent("\t+ ")}
        """.trimMargin()
    }
}
