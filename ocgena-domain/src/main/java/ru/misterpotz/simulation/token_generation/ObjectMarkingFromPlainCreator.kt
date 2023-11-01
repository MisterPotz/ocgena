package ru.misterpotz.simulation.token_generation

import model.PlaceTyping
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.simulation.queue.TokenGenerationFacade
import javax.inject.Inject

class ObjectMarkingFromPlainCreator @Inject constructor(
    private val plainMarking: PlainMarking,
    private val placeTyping: PlaceTyping,
    private val tokenGenerationFacade: TokenGenerationFacade
) {
    fun create(): ImmutableObjectMarking {
        return ImmutableObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            add(tokenGenerationFacade.generate(placeTyping[place]).id)
                        }
                    }.toSortedSet())
                }
            }.toMutableMap()
        )
    }
}
