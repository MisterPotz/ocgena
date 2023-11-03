package ru.misterpotz.ocgena.simulation.config

interface Config {
    val type : ConfigEnum
    fun toDebugString() : String
}

interface ConfigHolder1 {
    fun toDebugString() : String
    fun getConfig(configEnum: ConfigEnum) : Config?
}

class ConfigHolder(val configs: List<Config>) {

    fun serialize(): Any {
        return configs.map { it.toDebugString() }.joinToString(separator = "\r\n")
    }

    fun getConfig(configEnum: ConfigEnum): Config? {
        val value = configs.find { it.type == configEnum }
        return value
    }
}

enum class ConfigEnum {
    INPUT_PLACES,
    OUTPUT_PLACES,
    PLACE_TYPING,
    LABEL_MAPPING,

    //    OBJECT_TYPES,
    INITIAL_MARKING,
    TRANSITIONS,
    OC_TYPE,
    RANDOM,
    GENERATION
}
