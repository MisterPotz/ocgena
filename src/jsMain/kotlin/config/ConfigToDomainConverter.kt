package config

import kotlinx.js.Object
import model.*
import model.time.IntervalFunction
import model.time.TransitionTimes
import simulation.PlainMarking
import simulation.ProcessedSimulationConfig
import simulation.config.ConfigEnum
import simulation.config.SimulationConfig


actual class SimulationConfigProcessor actual constructor(
    private val simulationConfig: SimulationConfig
) {
    actual fun createProcessedConfig(): ProcessedSimulationConfig {
        return ProcessedSimulationConfig(
            placeTyping = getPlaceTyping(),
            inputOutputPlaces = getInputOutputPlaces(),
            type = getOcNetType(),
            initialPlainMarking = getInitialMarking(),
            intervalFunction = getIntervalFunction(),
            labelMapping = getLabelMapping()
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
        return (simulationConfig.getConfig(ConfigEnum.OC_TYPE) as? OCNetTypeConfig)?.ocNetType ?: OcNetType.TYPE_A
    }

    private fun getInitialMarking(): PlainMarking {
        val configMarking = (simulationConfig.getConfig(ConfigEnum.INITIAL_MARKING) as? InitialMarkingConfig)

        val plainMarking = PlainMarking.of {
            if (configMarking != null) {
                val placeIdToTokens = configMarking.placeIdToInitialMarking
                val places = Object.keys(placeIdToTokens as Any)
                for (place in places) {
                    val tokensAmount = placeIdToTokens[place]
                    put(place, tokensAmount as Int)
                }
            }
        }
        return plainMarking
    }

    private fun getLabelMapping() : LabelMapping {
        val labelMappingConfig = (simulationConfig.getConfig(ConfigEnum.LABEL_MAPPING) as? LabelMappingConfig)
        val labels = labelMappingConfig?.placeIdToLabel

        return LabelMapping.create {
            if (labels != null) {
                for (id in Object.keys(labels as Any)) {
                    val label = labels[id]

                    put(id, label.toString())
                }
            }
        }
    }

    private fun getIntervalFunction(): IntervalFunction {
        val transitionIntervalConfig = (simulationConfig.getConfig(ConfigEnum.TRANSITIONS) as? TransitionsConfig)

        if (transitionIntervalConfig != null) {
            return IntervalFunction(
                defaultTransitionTimes = if (transitionIntervalConfig.defaultTransitionInterval != null) {
                    mapTransitionIntervalToDomain(transitionIntervalConfig.defaultTransitionInterval)
                } else  TransitionTimes(duration = 0..0, pauseBeforeNextOccurence = 0..0),

                buildMap {
                    val transitionsToIntervals = transitionIntervalConfig.transitionsToIntervals
                    val transitions = Object.keys(transitionsToIntervals as Any)

                    for (transition in transitions) {
                        val transitionTimes = mapTransitionIntervalToDomain(
                            transitionsToIntervals[transition]
                        )
                        put(transition, transitionTimes)
                    }
                }.toMutableMap()
            )
        }

        return IntervalFunction(
            defaultTransitionTimes = TransitionTimes(duration = 0..0, pauseBeforeNextOccurence = 0..0)
        )
    }

    private fun mapTransitionIntervalToDomain(obj: dynamic): TransitionTimes {
        val durationJs = obj.duration
        val duration = mapInterval(durationJs)
        val minOccurrenceInterval = mapInterval(obj.minOccurrenceInterval)
        return TransitionTimes(duration, minOccurrenceInterval)
    }

    private fun mapInterval(obj: dynamic): IntRange {
        val start = obj.start
        val end = obj.end
        return start.toString().toInt()..(end.toString().toInt())
    }
}
