package simulation

import model.InputOutputPlaces
import model.LabelMapping
import model.OcNetType
import model.PlaceTyping
import model.time.IntervalFunction

class ProcessedSimulationConfig(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val type: OcNetType,
    val initialPlainMarking: PlainMarking,
    val intervalFunction: IntervalFunction,
    val labelMapping : LabelMapping,
)
