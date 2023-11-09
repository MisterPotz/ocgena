package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.ObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

interface TokenSelectionInteractor {
    fun getTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int
    ): SortedSet<ObjectTokenId>

    fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId>
    fun shuffleTokens(tokens: SortedSet<ObjectTokenId>): SortedSet<ObjectTokenId>
}

class TokenSelectionInteractorImpl @Inject constructor(
    private val random: Random?,
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val newTokenGenerationFacade: NewTokenGenerationFacade,
    private val pMarkingProvider: PMarkingProvider,
    private val objectTokenSet: ObjectTokenSet,
    private val ocNet : OCNet,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
) : TokenSelectionInteractor {
    private val objectTypeRegistry : ObjectTypeRegistry = ocNet.objectTypeRegistry
    val pMarking get() = pMarkingProvider.get()

    override fun getTokensFromPlace(
        placeId: PetriAtomId,
//        set: SortedSet<ObjectTokenId>,
        amount: Int
    ): SortedSet<ObjectTokenId> {
        val markingAtPlace = pMarking[placeId]

        val existingTokensSize = objectTokenRealAmountRegistry.getRealAmountAt(placeId)
        val totalTokensOfType = objectTokenRealAmountRegistry.getRealAmountOfType(placeId)
        val existing = markingAtPlace?.size ?: 0

        require(realSize >= amount) {
            "can't request from a place more tokens than the real amount of them"
        }

        return random?.let {
            set.shuffled(random = it)
                .take(amount)
                .toSortedSet()
        } ?: set
            .take(amount)
            .toSortedSet()
    }

    fun generateRandomObjectsInRange(amount: Int, type: ObjectTypeId): List<ObjectTokenId> {
        return if (random == null) {
            (0..<amount).map {
                newTokenGenerationFacade.generateRealToken(type).id
            }
        } else {
            val mutableSet = mutableSetOf<ObjectTokenId>()
            val range : IntRange = (0..<amount)

            val totalObjectTokensPerType = objectTokenSet.get(type)
            var nextIndex = range.random()

            while (mutableSet.size < amount) {
                val random = range.random()

                if (random)
                mutableSet.add(random)
            }

            mutableSet.toList()
        }

    }

    override fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId> {
        if (random == null) return repeatabilityInteractor.sortTokens(tokens)
        return repeatabilityInteractor.sortTokens(tokens).shuffled(random)
    }

    override fun shuffleTokens(tokens: SortedSet<ObjectTokenId>): SortedSet<ObjectTokenId> {
        if (random == null) return tokens

        return tokens.shuffled(random).toSortedSet()
    }
}
