package config

import converter.ConfigProcessingResult
import model.InputOutputPlaces
import model.OcNetType
import model.PlaceTyping

class ConfigToDomainConverter(
    private val simulationConfig: SimulationConfig
) {
    fun processAll(): ConfigProcessingResult {
        return ConfigProcessingResult(
            placeTyping = getPlaceTyping(),
            inputOutputPlaces = getInputOutputPlaces(),
            type = getOcNetType()
        )
    }

    private fun getPlaceTyping(): PlaceTyping {
        val placeTypingConfig = simulationConfig.getConfig(ConfigEnum.PLACE_TYPING) as? PlaceTypingConfig
            ?: return PlaceTyping.build { }

        val allObjectTypes = placeTypingConfig.objectTypes()
        return PlaceTyping.build {
            for (i in allObjectTypes) {
                objectType(i, placeTypingConfig.forObjectType(i))
            }
        }
    }

    private fun getInputOutputPlaces(): InputOutputPlaces {
        val inputPlaces = simulationConfig.getConfig(ConfigEnum.INPUT_PLACES) as? InputPlacesConfig
        val outputPlaces = simulationConfig.getConfig(ConfigEnum.OUTPUT_PLACES) as? OutputPlacesConfig

        return InputOutputPlaces.build {
            if (inputPlaces != null) {
                inputPlaces(inputPlaces.inputPlaces)
            }
            if (outputPlaces != null) {
                outputPlaces(outputPlaces.outputPlaces)
            }
        }
    }

    private fun getOcNetType(): OcNetType {
        return simulationConfig.getConfig(ConfigEnum.OC_TYPE)!!.type.let {
            OcNetType.values()[it]
        }
    }
}
