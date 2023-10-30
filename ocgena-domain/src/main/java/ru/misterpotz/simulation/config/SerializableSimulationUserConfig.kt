package ru.misterpotz.simulation.config

import config.*
import kotlinx.serialization.Serializable
import model.OcNetType

@Serializable
data class SerializableSimulationUserConfig(
    val inputPlaces: PlacesConfig?, /* p1 p2 p3 */
    val outputPlaces: PlacesConfig?, /*p1 p2 p3 */
    val ocNetType: OcNetType?,
    val randomization: RandomizationConfig?,
    val generation: GenerationConfig?,
    val placeTyping: PlaceTypingConfig?,
    val initialMarking: InitialMarkingConfig?,
    val transitionTimes: TransitionTimesConfig?,
)
