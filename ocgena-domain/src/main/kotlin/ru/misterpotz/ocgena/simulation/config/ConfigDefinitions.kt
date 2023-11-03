package config;

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import simulation.config.Config
import simulation.config.ConfigEnum

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

@Serializable
data class PlaceTypingConfig(private val objectTypeIdToPlaceId: Map<String, String>) : Config {
    fun objectTypes(): List<ObjectTypeId> {
        return objectTypeIdToPlaceId.keys.toList()
    }

    override fun toDebugString(): String {
        return objectTypeIdToPlaceId.entries.joinToString(
            separator = "; ",
            prefix = "[place typing: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
        }
    }

    fun forObjectType(objectTypeId: ObjectTypeId): String {
        return objectTypeIdToPlaceId[objectTypeId] as String
    }

    override val type = ConfigEnum.PLACE_TYPING

    companion object {
        fun fastCreate(string: String): PlaceTypingConfig {
            return PlaceTypingConfig(parseStringToMap(string))
        }
    }
}

typealias PlacesConfig = List<String>

@Serializable
data class LabelMappingConfig(val placeIdToLabel: Map<String, String>) : Config {
    override val type = ConfigEnum.LABEL_MAPPING
    override fun toDebugString(): String {
        return placeIdToLabel.entries.joinToString(
            separator = "; ",
            prefix = "[label mapping: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
        }
    }

    companion object {
        fun fastCreate(string: String): LabelMappingConfig {
            return LabelMappingConfig(parseStringToMap(string))
        }
    }
}

@Serializable
data class InitialMarkingConfig(
    val placeIdToInitialMarking: Map<String, Int> /* map place id to initial marking (int) */
) : Config {
    override val type = ConfigEnum.INITIAL_MARKING
    override fun toDebugString(): String {
        return placeIdToInitialMarking.entries.joinToString(
            separator = "; ",
            prefix = "[initial marking: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
        }
    }

    companion object {
        fun fastCreate(string: String): InitialMarkingConfig {
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
    val pattern =
        Regex("""(\w+):\s+(?:d\[(\d+),\s*(\d+)\]\s+min\[(\d+),\s*(\d+)\]|min\[(\d+),\s*(\d+)\]\s+d\[(\d+),\s*(\d+)\])""")

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

fun mapToTransitionsIntervals(map: Map<String, List<String>>): TransitionIntervals {
    return TransitionIntervals(
        duration = TimeRange(map["d"]!!.map { it.toInt() }),
        minOccurrenceInterval = TimeRange(map["min"]!!.map { it.toInt() })
    )
}

fun TimeRange.customString(): String {
    return "[$start, $end]"
}

fun TransitionIntervals.customString(): String {
    return "d${duration?.customString()}; min${minOccurrenceInterval?.customString()}"
}

@Serializable
data class TransitionTimesConfig(
    val defaultTransitionInterval: TransitionIntervals?,
    val transitionsToIntervals: Map<String, TransitionIntervals>,
) : Config {
    fun getTransitionConfig(transitionId: PetriAtomId): TransitionIntervals {
        return transitionsToIntervals[transitionId] as TransitionIntervals
    }

    override fun toDebugString(): String {
        return "[default transition interval: ${defaultTransitionInterval?.customString()} ] " + transitionsToIntervals.entries.joinToString(
            separator = "; ",
            prefix = "[transitions intervals: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
        }
    }

    override val type = ConfigEnum.TRANSITIONS

    companion object {
        fun fastCreate(defaultExpression: String?, mapIntervals: String?): TransitionTimesConfig {
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
            return TransitionTimesConfig(defaultExpression, mapExpression ?: emptyMap())
        }
    }
}

@Serializable
data class TimeRange(val timeRange: List<Int>) {
    val start: Int
        get() = timeRange.first()
    val end: Int
        get() = timeRange[1]
}

@Serializable
data class TransitionIntervals(
    val duration: TimeRange?,
    val minOccurrenceInterval: TimeRange?
)

@Serializable
data class GenerationConfig(
    val defaultGeneration: TimeRange?,
    val placeIdToGenerationTarget: Map<String, Int> /* map place id to initial marking (int) */
) : Config {
    override val type: ConfigEnum = ConfigEnum.GENERATION

    override fun toDebugString(): String {
        return "[default generation: ${defaultGeneration?.customString()} ] " + placeIdToGenerationTarget.entries.joinToString(
            separator = "; ",
            prefix = "[transitions intervals: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
        }
    }

    companion object {
        fun fastCreate(defaultGeneration: String?, placeIdToGenerationTarget: String?): GenerationConfig {
            return GenerationConfig(
                defaultGeneration = defaultGeneration?.let { parseInterval(it) },
                placeIdToGenerationTarget = placeIdToGenerationTarget?.let { parseStringToMapInt(it) } ?: mapOf()
            )
        }
    }
}

@Serializable
data class RandomizationConfig(
    val turnOn: Boolean = true,
    val seed: Int? = null
) : Config {
    override val type: ConfigEnum = ConfigEnum.RANDOM
    override fun toDebugString(): String {
        return "[random turnOn: $turnOn; seed: $seed]"
    }

    companion object {
        fun fastCreate(string: String): RandomizationConfig {
            val map = parseStringToMap(string)
            return RandomizationConfig(
                map["turnOn"]?.toBoolean() ?: true,
                seed = map["seed"]?.toInt()
            )
        }
    }
}

fun parseInterval(string: String): TimeRange {
    val values = string
        .removePrefix("[")
        .removeSuffix("]")
        .split(",")
        .map { it.trim().toInt() }
    return TimeRange(values)
}
