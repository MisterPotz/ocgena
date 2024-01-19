package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.interactors.ArcPrePlaceHasEnoughTokensChecker
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.utils.DefinitionRef

interface PrePlaceRegistry {
    fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId>

    //    fun getPostPlaces(transition: PetriAtomId): Set<PetriAtomId>
    @DefinitionRef("t^-")
    fun transitionPrePlaces(transition: PetriAtomId): PrePlaceAccessor

    interface PrePlaceAccessor : Iterable<PetriAtomId> {
        val transitionId : PetriAtomId

        operator fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int
    }

    interface PostPlaceAccessor {
        operator fun compareTo(objectMarking: PlaceToObjectMarking): Int
    }
}

class PrePlaceRegistryImpl(
    private val preplaceMap: Map<PetriAtomId, Set<PetriAtomId>>,
//    private val postplaceMap: MutableMap<PetriAtomId, Set<PetriAtomId>>,
    private val preplaceAccessorCache: MutableMap<PetriAtomId, PrePlaceRegistry.PrePlaceAccessor> = mutableMapOf(),
    private val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
) : PrePlaceRegistry {
    override fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId> {
        return preplaceMap[transition]!!
    }

//    override fun getPostPlaces(transition: PetriAtomId): Set<PetriAtomId> {
//        return postplaceMap[transition]!!
//    }

    override fun transitionPrePlaces(transition: PetriAtomId): PrePlaceRegistry.PrePlaceAccessor {
        return preplaceAccessorCache.getOrPut(transition) {
            PrePlaceAccessorImpl(getPrePlaces(transition), transition, arcPrePlaceHasEnoughTokensChecker)
        }
    }

    private class PrePlaceAccessorImpl(
        val places: Set<PetriAtomId>,
        override val transitionId: PetriAtomId,
        val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
    ) : PrePlaceRegistry.PrePlaceAccessor, Iterable<PetriAtomId> by places {

        override fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int {
            val allHasRequiredTokens = places.all {
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens(
                    it,
                    transitionId,
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
        ): PrePlaceRegistry {
            val preplaceMap = buildMap<PetriAtomId, Set<PetriAtomId>> {
                for (transition in ocNet.transitionsRegistry.iterable) {
                    val inputPlaces = transition.fromPlaces
                    put(transition.id, inputPlaces.toSet())
                }
            }
            return PrePlaceRegistryImpl(
                preplaceMap,
                arcPrePlaceHasEnoughTokensChecker = arcPrePlaceHasEnoughTokensChecker
            )
        }
    }
}