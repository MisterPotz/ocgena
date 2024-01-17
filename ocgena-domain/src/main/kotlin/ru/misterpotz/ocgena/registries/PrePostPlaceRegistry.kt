package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.interactors.ArcPrePlaceHasEnoughTokensChecker
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.DefinitionRef

interface PrePostPlaceRegistry {
    fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId>

    //    fun getPostPlaces(transition: PetriAtomId): Set<PetriAtomId>
    @DefinitionRef("t^-")
    fun transitionPrePlaces(transition: PetriAtomId): PrePlaceAccessor

    interface PrePlaceAccessor {
        operator fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int
    }

    interface PostPlaceAccessor {
        operator fun compareTo(objectMarking: PlaceToObjectMarking): Int
    }
}

class PrePostPlaceRegistryImpl(
    private val preplaceMap: Map<PetriAtomId, Set<PetriAtomId>>,
//    private val postplaceMap: MutableMap<PetriAtomId, Set<PetriAtomId>>,
    private val preplaceAccessorCache: MutableMap<PetriAtomId, PrePostPlaceRegistry.PrePlaceAccessor> = mutableMapOf(),
    private val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
) : PrePostPlaceRegistry {
    override fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId> {
        return preplaceMap[transition]!!
    }

//    override fun getPostPlaces(transition: PetriAtomId): Set<PetriAtomId> {
//        return postplaceMap[transition]!!
//    }

    override fun transitionPrePlaces(transition: PetriAtomId): PrePostPlaceRegistry.PrePlaceAccessor {
        return preplaceAccessorCache.getOrPut(transition) {
            PrePlaceAccessorImpl(getPrePlaces(transition), transition, arcPrePlaceHasEnoughTokensChecker)
        }
    }

    private class PrePlaceAccessorImpl(
        val places: Set<PetriAtomId>,
        val transition: PetriAtomId,
        val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
    ) : PrePostPlaceRegistry.PrePlaceAccessor {
        override fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int {
            val allHasRequiredTokens = places.all {
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens(
                    it,
                    transition,
                    objectTokenRealAmountRegistry
                )
            }
            return if (allHasRequiredTokens) {
                0
            } else {
                1
            }
        }
    }

    companion object {
        fun create(
            ocNet: OCNet,
            arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
        ): PrePostPlaceRegistry {
            val preplaceMap = buildMap<PetriAtomId, Set<PetriAtomId>> {
                for (transition in ocNet.transitionsRegistry.iterable) {
                    val inputPlaces = transition.fromPlaces
                    put(transition.id, inputPlaces.toSet())
                }
            }
            return PrePostPlaceRegistryImpl(
                preplaceMap,
                arcPrePlaceHasEnoughTokensChecker = arcPrePlaceHasEnoughTokensChecker
            )
        }
    }
}