@file:OptIn(ExperimentalJsExport::class)
package config

import kotlinx.js.Object
import model.ObjectTypeId
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
abstract class Config() {
    abstract val type : Int
}

@JsExport
class InputPlacesConfig(val inputPlaces: String) : Config() {
    override val type: Int = ConfigEnum.INPUT_PLACES.ordinal
}

@JsExport
class OutputPlacesConfig(val outputPlaces : String): Config() {
    override val type: Int = ConfigEnum.OUTPUT_PLACES.ordinal
}

@JsExport
class LabelMapping(val placeIdToLabel : dynamic): Config() {
    override val type: Int = ConfigEnum.LABEL_MAPPING.ordinal
}
@JsExport
class PlaceTypingConfig(val objectTypeIdToPlaceId: dynamic) : Config() {
    fun objectTypes() : List<ObjectTypeId> {
        return Object.keys(objectTypeIdToPlaceId).toList()
    }

    fun forObjectType(objectTypeId: ObjectTypeId) : String {
        return objectTypeIdToPlaceId[objectTypeId] as String
    }

    override val type: Int = ConfigEnum.PLACE_TYPING.ordinal
}


@JsExport
class InitialMarkingConfig(
    val placeIdToInitialMarking : dynamic /* map place id to initial marking (int) */
) : Config() {
    override val type: Int = ConfigEnum.INITIAL_MARKING.ordinal
}

@JsExport
class TimeRange(
    val start : Int,
    val end : Int
)

@JsExport
class TransitionConfig(
    val duration: TimeRange,
    val minOccurrenceInterval : TimeRange,
) {
}

@JsExport
class TransitionsConfig(
    val transitionsToConfig : dynamic /* map transition id to transition config */
) : Config() {
    fun getTransitionConfig(transitionId : TransitionId) : TransitionConfig {
        return transitionsToConfig[transitionId] as TransitionConfig
    }

    override val type: Int = ConfigEnum.TRANSITIONS.ordinal
}

@JsExport
class SimulationConfig(val configs: Array<Config>) {

    fun getConfig(configEnum: ConfigEnum) : Config? {
        val value = configs.find { it.type == configEnum.ordinal }
        return value
    }
}
