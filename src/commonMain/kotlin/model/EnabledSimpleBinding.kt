package model

class ExecutedBinding(
    val finishedTransition : ActiveFiringTransition,
    val consumedMap : ObjectMarking,
    val producedMap : ObjectMarking
)
