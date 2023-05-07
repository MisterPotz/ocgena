@file:OptIn(ExperimentalJsExport::class)

package config

import kotlinx.js.Object
import model.ObjectTypeId
import model.OcNetType
import model.TransitionId
import simulation.config.Config
import simulation.config.ConfigEnum
import simulation.config.SimulationConfig


@JsExport
class InputPlacesConfig(val inputPlaces: String) : Config() {
    override val type: Int = ConfigEnum.INPUT_PLACES.ordinal
}

@JsExport
class OutputPlacesConfig(val outputPlaces: String) : Config() {
    override val type: Int = ConfigEnum.OUTPUT_PLACES.ordinal
}

@JsExport
class OCNetTypeConfig(val ocNetType: Int/* OcNetType */) : Config() {
    override val type: Int = ConfigEnum.OC_TYPE.ordinal
    companion object {
        fun from(ocNetType: OcNetType) : OCNetTypeConfig {
            return OCNetTypeConfig(ocNetType.ordinal)
        }
    }
}


@JsExport
class PlaceTypingConfig(val objectTypeIdToPlaceId: dynamic) : Config() {
    fun objectTypes(): List<ObjectTypeId> {
        return Object.keys(objectTypeIdToPlaceId).toList()
    }

    fun forObjectType(objectTypeId: ObjectTypeId): String {
        return objectTypeIdToPlaceId[objectTypeId] as String
    }

    override val type: Int = ConfigEnum.PLACE_TYPING.ordinal
}

@JsExport
class LabelMappingConfig(val placeIdToLabel: dynamic) : Config() {
    override val type: Int = ConfigEnum.LABEL_MAPPING.ordinal
}

@JsExport
class InitialMarkingConfig(
    val placeIdToInitialMarking: dynamic /* map place id to initial marking (int) */
) : Config() {
    override val type: Int = ConfigEnum.INITIAL_MARKING.ordinal
}

@JsExport
interface TimeRange {
    val start: Int
    val end: Int
}

class TimeRangeImp(override val start: Int, override val end: Int) : TimeRange

@JsExport
interface TransitionIntervals {
    val duration: TimeRange
    val minOccurrenceInterval: TimeRange
}

class TransitionIntervalsImp(override val duration: TimeRange, override val minOccurrenceInterval: TimeRange) :
    TransitionIntervals

@JsExport
class TransitionsConfig(
    val defaultTransitionInterval : TransitionIntervals?,
    val transitionsToIntervals: dynamic /* map transition id to transition config */
) : Config() {
    fun getTransitionConfig(transitionId: TransitionId): TransitionIntervals {
        return transitionsToIntervals[transitionId] as TransitionIntervals
    }

    override val type: Int = ConfigEnum.TRANSITIONS.ordinal
}

fun createConfig(
    ocNetTypeConfig: OCNetTypeConfig,
    inputPlacesConfig: InputPlacesConfig,
    outputPlacesConfig: OutputPlacesConfig,
    initialMarkingConfig: InitialMarkingConfig,
    transitionIntervalsConfig : TransitionsConfig? = null,
    labelMappingConfig: LabelMappingConfig? = null
): SimulationConfig {
    return SimulationConfig(
        buildList {
            add(ocNetTypeConfig)
            add(inputPlacesConfig)
            add(outputPlacesConfig)
            add(initialMarkingConfig)
            if (transitionIntervalsConfig != null) {
                add(transitionIntervalsConfig)
            }
            if (labelMappingConfig != null) {
                add(labelMappingConfig)
            }
        }.toTypedArray()
    )
}

