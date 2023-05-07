package converter

import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping

class ParseProcessorParams(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val netType : OcNetType
)
