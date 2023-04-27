package config

import converter.ConfigProcessingResult
import model.InputOutputPlaces
import model.PlaceTyping

class ConfigToDomainConverter(
    private val simulationConfig: SimulationConfig
) {
    fun processAll() : ConfigProcessingResult {
        return ConfigProcessingResult(
            placeTyping = getPlaceTyping(),
            inputOutputPlaces = getInputOutputPlaces()
        )
    }

    fun getPlaceTyping(): PlaceTyping {
        val placeTypingConfig = simulationConfig.getConfig(ConfigEnum.PLACE_TYPING) as PlaceTypingConfig
        placeTypingConfig

        val allObjectTypes = placeTypingConfig.objectTypes()
        return PlaceTyping.build {
            for (i in allObjectTypes) {
                objectType(i, placeTypingConfig.forObjectType(i))
            }
        }
    }

    fun getInputOutputPlaces() : InputOutputPlaces {
        val inputPlaces = simulationConfig.getConfig(ConfigEnum.INPUT_PLACES)!! as InputPlacesConfig
        val outputPlaces = simulationConfig.getConfig(ConfigEnum.OUTPUT_PLACES)!! as OutputPlacesConfig

        return InputOutputPlaces.build {
            inputPlaces(inputPlaces.inputPlaces)
            outputPlaces(outputPlaces.outputPlaces)
        }
    }
}
