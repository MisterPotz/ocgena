package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.registries.ObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerationFacade
import javax.inject.Inject

class PlaceToObjectMarkingBySchemeCreatorFactory @Inject constructor(
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade,
    private val objectTypeRegistry: ObjectTypeRegistry,
) {
    fun create(markingScheme: MarkingScheme): PlaceToObjectMarkingBySchemeCreator {
        return PlaceToObjectMarkingBySchemeCreator(
            markingScheme,
            placeToObjectTypeRegistry,
            objectTypeRegistry,
            newTokenTimeBasedGenerationFacade
        )
    }
}

class PlaceToObjectMarkingBySchemeCreator @Inject constructor(
    private val plainMarking: MarkingScheme,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val objectTypeRegistry: ObjectTypeRegistry,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
) {
    fun create(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        return ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            val objectTypeID = placeToObjectTypeRegistry[place]
                            val objectType = objectTypeRegistry[objectTypeID]
                            add(newTokenTimeBasedGenerationFacade.generate(objectType).id)
                        }
                    }.toSortedSet())
                }
            }.toMutableMap()
        )
    }
}
