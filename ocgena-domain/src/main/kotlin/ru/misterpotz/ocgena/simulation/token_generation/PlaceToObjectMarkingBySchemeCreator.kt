package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerationFacade
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import javax.inject.Inject

class PlaceToObjectMarkingBySchemeCreatorFactory @Inject constructor(
    currentSimulationDelegate: CurrentSimulationDelegate,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade,
) {
    private val placeToObjectTypeRegistry = currentSimulationDelegate.placeToObjectTypeRegistry

    fun create(markingScheme: MarkingScheme): PlaceToObjectMarkingBySchemeCreator {
        return PlaceToObjectMarkingBySchemeCreator(
            markingScheme,
            placeToObjectTypeRegistry,
            newTokenTimeBasedGenerationFacade
        )
    }
}

class PlaceToObjectMarkingBySchemeCreator @Inject constructor(
    private val plainMarking: MarkingScheme,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
) {
    fun create(): ImmutablePlaceToObjectMarking {
        return ImmutablePlaceToObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            val objectTypeID = placeToObjectTypeRegistry[place]
                            add(newTokenTimeBasedGenerationFacade.generate(objectTypeID).id)
                        }
                    }.toSortedSet())
                }
            }.toMutableMap()
        )
    }
}
