@file:OptIn(ExperimentalJsExport::class)

package config

import kotlinx.js.Object
import simulation.config.Config

@JsExport
fun toPlaceTypingConfig(objectTypeIdToPlaceId: dynamic) : Config {
    return PlaceTypingConfig(
        buildMap {
            for (key in Object.keys(objectTypeIdToPlaceId as Any)) {
                put(key, objectTypeIdToPlaceId[key] as String)
            }
        }
    )
}

@JsExport
fun toLabelMappingConfig(placeIdToLabel: dynamic) : Config {
    return LabelMappingConfig(
        buildMap {
            for (key in Object.keys(placeIdToLabel as Any)) {
                put(key, placeIdToLabel[key] as String)
            }
        }
    )
}

@JsExport
fun toInitialMarkingConfig(placeIdToInitialMarking: dynamic) : Config {
    return InitialMarkingConfig(
        buildMap {
            for (key in Object.keys(placeIdToInitialMarking as Any)) {
                put(key, placeIdToInitialMarking[key] as Int)
            }
        }
    )
}

@JsExport
external interface JsTransitionIntervals {
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

@JsExport
fun toTransitionsConfig(
    defaultTransitionInterval: TransitionIntervals?,
    transitionsToIntervals: dynamic
): Config {
    return TransitionsConfig(
        defaultTransitionInterval,
        buildMap {
            for (key in Object.keys(transitionsToIntervals as Any)) {
                put(key, transitionsToIntervals[key] as TransitionIntervals)
            }
        }
    )
}
