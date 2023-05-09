package simulation.config

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
abstract class Config() {
    abstract val type: Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class SimulationConfig(val configs: Array<Config>) {

    fun getConfig(configEnum: ConfigEnum): Config? {
        val value = configs.find { it.type == configEnum.ordinal }
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
    OC_TYPE
}
