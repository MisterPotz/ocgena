@file:OptIn(ExperimentalJsExport::class)
package config;

import model.ObjectTypeId
import model.OcNetType
import model.TransitionId
import simulation.config.Config
import simulation.config.ConfigEnum
import simulation.config.SimulationConfig
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@JsExport()
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

fun parseStringToMap(input: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val pairs = input.split(Regex("[;\n]+"))
    for (pair in pairs) {
        val parts = pair.split(":")
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()
            map[key] = value
        }
    }
    return map
}

fun parseStringToMapInt(input: String): Map<String, Int> {
    val map = mutableMapOf<String, Int>()
    val pairs = input.split(Regex("[;\n]+"))
    for (pair in pairs) {
        val parts = pair.split(":")
        if (parts.size == 2) {
            val key = parts[0].trim()
            val values = parts[1].trim().toInt()
            map[key] = values
        }
    }
    return map
}


class PlaceTypingConfig(private val objectTypeIdToPlaceId: Map<String, String>) : Config() {
    fun objectTypes(): List<ObjectTypeId> {
        return objectTypeIdToPlaceId.keys.toList()
    }

    fun forObjectType(objectTypeId: ObjectTypeId): String {
        return objectTypeIdToPlaceId[objectTypeId] as String
    }

    override val type = ConfigEnum.PLACE_TYPING

    companion object {
        fun fastCreate(string: String) : PlaceTypingConfig {
            return PlaceTypingConfig(parseStringToMap(string))
        }
    }
}

class LabelMappingConfig(val placeIdToLabel: Map<String, String>) : Config() {
    override val type = ConfigEnum.LABEL_MAPPING

    companion object {
        fun fastCreate(string: String) : LabelMappingConfig {
            return LabelMappingConfig(parseStringToMap(string))
        }
    }
}

class InitialMarkingConfig(
    val placeIdToInitialMarking: Map<String, Int> /* map place id to initial marking (int) */
) : Config() {
    override val type = ConfigEnum.INITIAL_MARKING

    companion object {
        fun fastCreate(string : String) : InitialMarkingConfig {
            return InitialMarkingConfig(
                parseStringToMapInt(string)
            )
        }
    }
}


fun parseTimeConfigExpression(expression: String): Map<String, Map<String, List<String>>> {
    val result = mutableMapOf<String, Map<String, List<String>>>()
    val pattern = Regex("""(\w+):\s+d\[(\d+),\s*(\d+)]\s+min\[(\d+),\s*(\d+)]""")
    val matches = pattern.findAll(expression)
    for (match in matches) {
        val key = match.groupValues[1]
        val values = mapOf(
            "d" to listOf(match.groupValues[2], match.groupValues[3]),
            "min" to listOf(match.groupValues[4], match.groupValues[5])
        )
        result[key] = values
    }
    return result
}

fun parseIntervalsExpression(expression: String): Map<String, Map<String, List<String>>> {
    val result = mutableMapOf<String, Map<String, List<String>>>()
//    val pattern = Regex("""(\w+):\s+(?:d\\[(\d+),\s*(\d+)\\]\s+min\\[(\d+),\s*(\d+)]|min\[(\d+),\s*(\d+)]\s+d\[(\d+),\s*(\d+)])""")
    val pattern = Regex("""(\w+):\s+(?:d\[(\d+),\s*(\d+)\]\s+min\[(\d+),\s*(\d+)\]|min\[(\d+),\s*(\d+)\]\s+d\[(\d+),\s*(\d+)\])""")

    val matches = pattern.findAll(expression)
    for (match in matches) {
        val key = match.groupValues[1]
        val values = if (match.groupValues[2].isNotEmpty()) {
            mapOf(
                "d" to listOf(match.groupValues[2], match.groupValues[3]),
                "min" to listOf(match.groupValues[4], match.groupValues[5])
            )
        } else {
            mapOf(
                "d" to listOf(match.groupValues[7], match.groupValues[8]),
                "min" to listOf(match.groupValues[6], match.groupValues[9])
            )
        }
        result[key] = values
    }
    return result
}

fun mapToTransitionsIntervals(map: Map<String, List<String>>) : TransitionIntervals {
    return object : TransitionIntervals {
        override val duration: TimeRange = TimeRangeClass(map["d"]!!.map { it.toInt() }.toTypedArray())
        override val minOccurrenceInterval: TimeRange = TimeRangeClass(map["min"]!!.map { it.toInt() }.toTypedArray())
    }
}

class TransitionsConfig(
    val defaultTransitionInterval : TransitionIntervals?,
    val transitionsToIntervals: Map<String, TransitionIntervals>,
) : Config() {
    fun getTransitionConfig(transitionId: TransitionId): TransitionIntervals {
        return transitionsToIntervals[transitionId] as TransitionIntervals
    }

    override val type = ConfigEnum.TRANSITIONS

    companion object {
        fun fastCreate(defaultExpression: String?, mapIntervals : String?) : TransitionsConfig {
            val defaultExpression = if (defaultExpression != null) {
                val parsed = parseIntervalsExpression(defaultExpression)
                mapToTransitionsIntervals(parsed["default"]!!)
            } else null
            val mapExpression = if (mapIntervals != null) {
                val parsed = parseIntervalsExpression(mapIntervals)
                parsed.keys.associate {
                    it to mapToTransitionsIntervals(parsed[it]!!)
                }
            } else null
            return TransitionsConfig(defaultExpression, mapExpression ?: emptyMap())
        }
    }
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

@JsExport
interface TransitionIntervals {
    val duration: TimeRange
    val minOccurrenceInterval: TimeRange
}

class GenerationConfig(val defaultGeneration : TimeRange)
@OptIn(ExperimentalJsExport::class)
@JsExport
class RandomConfig(val turnOn: Boolean = true,val seed : Int? = null) : Config() {
    override val type: ConfigEnum = ConfigEnum.RANDOM

    companion object {
        fun fastCreate(string: String): RandomConfig {
            val map = parseStringToMap(string)
            return RandomConfig(
                map["turnOn"]?.toBoolean() ?: true,
                seed = map["seed"]?.toInt()
            )
        }
    }
}

fun createConfigFast(
    ocNetTypeConfig: OcNetType,
    inputPlaces: String,
    outputPlaces: String,
    initialMarkingConfig: String,
    defaultTransitionIntervals: String? = null,
    transitionsIntervalsMap : String? = null,
    labelMapping: String? = null,
    randomSetting: String? = null
) : SimulationConfig {
    return createConfig(
        ocNetTypeConfig = OCNetTypeConfig(ocNetTypeConfig),
        inputPlacesConfig = InputPlacesConfig(inputPlaces),
        outputPlacesConfig = OutputPlacesConfig(outputPlaces),
        initialMarkingConfig = InitialMarkingConfig.fastCreate(initialMarkingConfig),
        transitionIntervalsConfig = TransitionsConfig.fastCreate(
            defaultTransitionIntervals,
            transitionsIntervalsMap
        ),
        labelMappingConfig = labelMapping?.let { LabelMappingConfig.fastCreate(it) }
    )
}
fun createConfig(
    ocNetTypeConfig: OCNetTypeConfig,
    inputPlacesConfig: InputPlacesConfig,
    outputPlacesConfig: OutputPlacesConfig,
    initialMarkingConfig: InitialMarkingConfig,
    transitionIntervalsConfig : TransitionsConfig? = null,
    labelMappingConfig: LabelMappingConfig? = null,
    randomConfig: RandomConfig? = null
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
            if (randomConfig != null) {
                add(randomConfig)
            }
        }.toTypedArray()
    )
}
