package simulation

import config.RandomConfig
import model.InputOutputPlaces
import model.LabelMapping
import model.OcNetType
import model.PlaceTyping
import model.time.IntervalFunction
import simulation.config.Config
import simulation.config.ConfigEnum
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

class ProcessedSimulationConfig(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val type: OcNetType,
    val initialPlainMarking: PlainMarking,
    val intervalFunction: IntervalFunction,
    val labelMapping : LabelMapping,
    val randomSettings: RandomConfig
)

