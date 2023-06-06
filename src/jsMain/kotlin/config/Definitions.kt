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
    override val type = ConfigEnum.INPUT_PLACES
}

@JsExport
class OutputPlacesConfig(val outputPlaces: String) : Config() {
    override val type = ConfigEnum.OUTPUT_PLACES
}

@JsExport
class OCNetTypeConfig(val ocNetType: OcNetType) : Config() {
    override val type = ConfigEnum.OC_TYPE
    companion object {
        fun from(ocNetType: OcNetType) : OCNetTypeConfig {
            return OCNetTypeConfig(ocNetType)
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

    override val type = ConfigEnum.PLACE_TYPING
}

@JsExport
class LabelMappingConfig(val placeIdToLabel: dynamic) : Config() {
    override val type = ConfigEnum.LABEL_MAPPING
}

@JsExport
class InitialMarkingConfig(
    val placeIdToInitialMarking: dynamic /* map place id to initial marking (int) */
) : Config() {
    override val type = ConfigEnum.INITIAL_MARKING
}

@JsExport
interface TimeRange {
    val start: Int
    val end: Int
}

@JsExport
class TimeRangeClass(private val array : Array<Int>) : TimeRange {
    override val start: Int
        get() = array.first()
    override val end: Int
        get() = array[1]
}

class TimeRangeImp(override val start: Int, override val end: Int) : TimeRange

@JsExport
external interface JsTransitionIntervals {
    val duration: TimeRange
    val minOccurrenceInterval: TimeRange
}

@JsExport
interface TransitionIntervals {
    val duration: TimeRange
    val minOccurrenceInterval: TimeRange
}

@JsExport
fun toTransitionIntervals(jsTransitionIntervals: JsTransitionIntervals) : TransitionIntervals = object : TransitionIntervals {
    override val duration: TimeRange
        get() = jsTransitionIntervals.duration
    override val minOccurrenceInterval: TimeRange
        get() = jsTransitionIntervals.minOccurrenceInterval
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

    override val type = ConfigEnum.TRANSITIONS
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

