package converter

import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping
import model.time.IntervalFunction
import simulation.PlainMarking

class ConfigProcessingResult(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val type: OcNetType,
    val initialPlainMarking: PlainMarking,
    val intervalFunction: IntervalFunction
)
