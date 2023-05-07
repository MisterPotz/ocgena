package converter

import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping

data class ConversionParams(
    val placeTyping: PlaceTyping,
    val inputOutputPlaces: InputOutputPlaces,
    val dslElementsContainer: StructureContainer,
    val useType: OcNetType
)
