package simulation.config

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class Config() {
    abstract val type: ConfigEnum
    abstract fun toSerializable() : Any
}

data class SerializableSimulationConfig(
    val serializableConfigs: List<Any>
) {

}

@OptIn(ExperimentalJsExport::class)
@JsExport
class SimulationConfig(val configs: Array<Config>) {

    fun serialize(): Any {
        return configs.map { it.toSerializable() }.joinToString(separator = "\r\n")
    }

    fun getConfig(configEnum: ConfigEnum): Config? {
        val value = configs.find { it.type == configEnum }
        return value
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
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
