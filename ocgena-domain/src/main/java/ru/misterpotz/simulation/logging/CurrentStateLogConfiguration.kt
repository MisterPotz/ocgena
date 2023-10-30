package ru.misterpotz.simulation.logging

class CurrentStateLogConfiguration(
    val includeOngoingTransitions: Boolean,
    val includeNextTransitionAllowedTiming: Boolean,
    val includePlaceMarking: Boolean
)