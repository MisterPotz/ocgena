package simulation

import model.ObjectMarking
import model.TMarking

class State : SimulatableComposedOcNet.State {
    override val tMarking: TMarking = TMarking()
    override val pMarking: ObjectMarking = ObjectMarking()

    override fun toString(): String {
        return """places:
            |${pMarking.toString().prependIndent("\t")}
            |transitions:
            |${tMarking.toString().prependIndent("\t")}
        """.trimMargin()
    }
}
