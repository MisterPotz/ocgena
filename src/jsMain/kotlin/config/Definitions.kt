package config

import model.PlaceId
import model.TransitionId

enum class ConfigEnum {
    INPUT_PLACES,
    OUTPUT_PLACES,
    PLACE_TYPING,
    LABEL_MAPPING,
    //    OBJECT_TYPES,
    INITIAL_MARKING,
    TRANSITIONS
}

@JsExport
open class Config()

@JsExport
class InputPlaces(val inputPlaces: Array<PlaceId>) : Config()

@JsExport
class OutputPlaces(val outputPlaces : Array<PlaceId>): Config()

@JsExport
class LabelMapping(val placeIdToLabel : dynamic): Config()

@JsExport
class PlaceTypingConfig(val objectTypeIdToPlaceId: dynamic) : Config() {
}


@JsExport
class InitialMarkingConfig(
    val placeIdToInitialMarking : dynamic /* map place id to initial marking (int) */
) : Config()

@JsExport
class TimeRange(
    val start : Int,
    val end : Int
)

@JsExport
class TransitionConfig(
    val duration: TimeRange,
    val minOccurrenceInterval : TimeRange,
) : Config()

@JsExport
class TransitionsConfig(
    val transitionsToConfig : dynamic /* map transition id to transition config */
) : Config() {
    fun getTransitionConfig(transitionId : TransitionId) : TransitionConfig {
        return transitionsToConfig[transitionId] as TransitionConfig
    }
}

@JsExport
class SimulationConfig(val configs: Array<Config>) {

}
