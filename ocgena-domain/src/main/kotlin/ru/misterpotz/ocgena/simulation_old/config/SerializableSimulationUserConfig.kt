package ru.misterpotz.ocgena.simulation_old.config

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType

@Serializable
data class SerializableSimulationUserConfig(
    val inputPlaces: PlacesConfig?, /* p1 p2 p3 */
    val outputPlaces: PlacesConfig?, /* p1 p2 p3 */
    val ocNetType: OcNetType?,
    val randomization: RandomizationConfig?,
    val generation: TokenGenerationConfig?,
    val placeTyping: PlaceTypingConfig?,
    val initialMarking: InitialMarkingConfig?,
    val transitionTimes: TransitionTimesConfig?,
)
