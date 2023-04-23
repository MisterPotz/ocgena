package simulation.binding

import model.ObjectMarking
import model.Transition

class EnabledBindingWithTokens(
    val transition: Transition,
    val involvedObjectTokens: ObjectMarking,
)
