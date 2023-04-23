package simulation

import model.ObjectMarking
import model.TMarking

class State : SimulatableComposedOcNet.State {
    override val tMarking: TMarking = TMarking()
    override val pMarking: ObjectMarking = ObjectMarking()
}
