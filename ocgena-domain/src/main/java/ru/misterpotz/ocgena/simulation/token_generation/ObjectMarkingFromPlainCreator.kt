package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.simulation.queue.TokenGenerationFacade
import javax.inject.Inject

class ObjectMarkingFromPlainCreator @Inject constructor(
    private val plainMarking: PlainMarking,
    private val placeObjectTypeRegistry: PlaceObjectTypeRegistry,
    private val tokenGenerationFacade: TokenGenerationFacade
) {
    fun create(): ImmutableObjectMarking {
        return ImmutableObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            add(tokenGenerationFacade.generate(placeObjectTypeRegistry[place]).id)
                        }
                    }.toSortedSet())
                }
            }.toMutableMap()
        )
    }
}
