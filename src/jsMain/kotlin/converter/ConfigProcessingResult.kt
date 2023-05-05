package converter

import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping

class ConfigProcessingResult(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val type: OcNetType,
)
