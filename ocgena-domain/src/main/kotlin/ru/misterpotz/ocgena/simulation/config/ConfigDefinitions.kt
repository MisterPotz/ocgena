package ru.misterpotz.ocgena.simulation.config;

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

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


fun Period.customString(): String {
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
}

@Serializable
data class Period(@Contextual val intRange: IntRange) {
    val start: Int
        get() = intRange.first()
    val end: Int
        get() = intRange.last
}

@Serializable
data class TransitionIntervals(
    val duration: Period?,
    val minOccurrenceInterval: Period?
)

@Serializable
data class TokenGenerationConfig(
    @Contextual
    val defaultPeriod: Period?,
    val placeIdToGenerationTarget: Map<String, Int>
) : Config {
    override val type: ConfigEnum = ConfigEnum.GENERATION

    override fun toDebugString(): String {
        return "[default generation: ${defaultPeriod?.customString()} ] " + placeIdToGenerationTarget.entries.joinToString(
            separator = "; ",
            prefix = "[transitions intervals: ",
            postfix = "]"
        ) {
            "${it.key}: ${it.value};"
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
