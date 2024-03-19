package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.interactors.ArcPrePlaceHasEnoughTokensChecker
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.utils.DefinitionRef

interface PrePlaceSynchronizedRegistry {
    fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId>

    @DefinitionRef("(t^-) with token synchronization consideration")
    fun transitionPrePlacesSynchronized(transition: PetriAtomId): PrePlaceRegistry.PrePlaceAccessor

    interface PrePlaceSynchronizedAccessor : Iterable<PetriAtomId> {
        val transitionId: PetriAtomId
        fun getTransitionsWithSharedPreplaces(): List<PetriAtomId>

        operator fun compareTo(objectTokens: SparseTokenBunch): Int
    }
}

class PrePlaceSynchronizedRegistryImpl() {

}

interface PrePlaceRegistry {
    fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId>

    @DefinitionRef("t^-")
    fun transitionPrePlaces(transition: PetriAtomId): PrePlaceAccessor

    interface PrePlaceAccessor : Iterable<PetriAtomId> {
        val transitionId: PetriAtomId
        fun getTransitionsWithSharedPreplaces(): List<PetriAtomId>

        operator fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int
    }
}

class PrePlaceRegistryImpl(
    private val preplaceMap: Map<PetriAtomId, Set<PetriAtomId>>,
    private val transitionsRegistry: TransitionsRegistry,
//    private val postplaceMap: MutableMap<PetriAtomId, Set<PetriAtomId>>,
    private val preplaceAccessorCache: MutableMap<PetriAtomId, PrePlaceRegistry.PrePlaceAccessor> = mutableMapOf(),
    private val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
) : PrePlaceRegistry {
    override fun getPrePlaces(transition: PetriAtomId): Set<PetriAtomId> {
        return preplaceMap[transition]!!
    }


    // в каком режиме работает preplaceaccessor? надо чтобы он мог сразу находить синхронизированные токены среди преплейсесов
    override fun transitionPrePlaces(transition: PetriAtomId): PrePlaceRegistry.PrePlaceAccessor {
        return preplaceAccessorCache.getOrPut(transition) {
            PrePlaceAccessorImpl(
                getPrePlaces(transition),
                transition,
                getTransitionsWithSharedPreplacesFor(transition),
                arcPrePlaceHasEnoughTokensChecker
            )
        }
    }

    private fun getTransitionsWithSharedPreplacesFor(transition: PetriAtomId): List<PetriAtomId> {
        val share = mutableSetOf<PetriAtomId>()
        val preplacesOfThisTransition = transitionsRegistry.get(transition).fromPlaces.toSet()

        for (transition in transitionsRegistry.iterable) {
            if (transition.fromPlaces.intersect(preplacesOfThisTransition).isNotEmpty()) {
                share.add(transition.id)
            }
        }
        return share.toList()
    }

    private class PrePlaceAccessorImpl(
        private val places: Set<PetriAtomId>,
        override val transitionId: PetriAtomId,
        private val transitionsWithSharedPreplaces: List<PetriAtomId>,
        private val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
    ) : PrePlaceRegistry.PrePlaceAccessor, Iterable<PetriAtomId> by places {

        override fun getTransitionsWithSharedPreplaces(): List<PetriAtomId> {
            return transitionsWithSharedPreplaces
        }

        override fun compareTo(objectTokenRealAmountRegistry: TokenAmountStorage): Int {
            val allHaveRequiredTokens = places.all {
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens(
                    it,
                    transitionId,
                    objectTokenRealAmountRegistry
                )
            }
            return if (allHaveRequiredTokens) {
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
            val preplaceMap = buildMap {
                for (transition in ocNet.transitionsRegistry.iterable) {
                    val inputPlaces = transition.fromPlaces
                    put(transition.id, inputPlaces.toSet())
                }
            }
            return PrePlaceRegistryImpl(
                preplaceMap,
                transitionsRegistry = ocNet.transitionsRegistry,
                arcPrePlaceHasEnoughTokensChecker = arcPrePlaceHasEnoughTokensChecker
            )
        }
    }
}