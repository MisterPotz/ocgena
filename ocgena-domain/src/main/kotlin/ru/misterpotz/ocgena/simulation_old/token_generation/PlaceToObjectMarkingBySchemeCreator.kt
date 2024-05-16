package ru.misterpotz.ocgena.simulation_old.token_generation

import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation_old.state.CurrentSimulationDelegate
import javax.inject.Inject

class PlaceToObjectMarkingBySchemeCreatorFactory @Inject constructor(
    currentSimulationDelegate: CurrentSimulationDelegate,
    private val newTokenGenerationFacade: NewTokenGenerationFacade,
) {
    private val placeToObjectTypeRegistry = currentSimulationDelegate.placeToObjectTypeRegistry

    fun create(markingScheme: MarkingScheme): PlaceToObjectMarkingBySchemeCreator {
        return PlaceToObjectMarkingBySchemeCreator(
            markingScheme,
            placeToObjectTypeRegistry,
            newTokenGenerationFacade
        )
    }
}

class PlaceToObjectMarkingBySchemeCreator @Inject constructor(
    private val plainMarking: MarkingScheme,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val newTokenGenerationFacade: NewTokenGenerationFacade
) {
    fun create(): ImmutablePlaceToObjectMarking {
        return ImmutablePlaceToObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            val objectTypeID = placeToObjectTypeRegistry[place]!!
                            add(newTokenGenerationFacade.generateRealToken(objectTypeID).id)
                        }
                    }.toSortedSet())
                }
            }.toMutableMap()
        )
    }
}
