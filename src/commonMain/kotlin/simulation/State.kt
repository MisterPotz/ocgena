package simulation

import model.ObjectMarking
import model.TMarking

class State : SimulatableComposedOcNet.State {
    override val tMarking: TMarking = TMarking()
    override val pMarking: ObjectMarking = ObjectMarking()

    override fun toString(): String {
        return """place marking:
            |${pMarking.toString().prependIndent("\t")}
            |transition timed marking:
            |${tMarking.toString().prependIndent("\t")}
        """.trimMargin()
    }
}
