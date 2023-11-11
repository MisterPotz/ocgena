package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation.state.PMarkingProvider
import ru.misterpotz.ocgena.utils.ByRandomRandomizer
import ru.misterpotz.ocgena.utils.NoOpIteratorRandomizer
import ru.misterpotz.ocgena.utils.RandomIterator
import ru.misterpotz.ocgena.utils.Randomizer
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

interface TokenSelectionInteractor {
    fun selectAndGenerateTokensFromPlace(
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
    ocNet: OCNet,
    private val objectTokenRealAmountRegistry: ObjectTokenRealAmountRegistry,
) : TokenSelectionInteractor {
    val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry

    val pMarking get() = pMarkingProvider.get()

    override fun selectAndGenerateTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int
    ): SortedSet<ObjectTokenId> {
        val markingAtPlace = pMarking[petriAtomId]
        val realTokenAmountAtPlace = objectTokenRealAmountRegistry.getRealAmountAt(petriAtomId)
        val existing = markingAtPlace.size
        fun indexBelongsToExisting(index: Int): Boolean {
            return index < existing
        }
        val sortedSet = sortedSetOf<ObjectTokenId>()
        val randomizer = createRandomizerOrNoOp()
        val objectTypeId = placeToObjectTypeRegistry[petriAtomId]
        val randomIterator = RandomIterator(amount = amount, randomizer = randomizer)

        require(realTokenAmountAtPlace >= amount) {
            "can't request from a place more tokens than the real amount of them"
        }
        val newlyGeneratedTokens = sortedSetOf<ObjectTokenId>()

        while (randomIterator.hasNext()) {
            val randomIndex = randomIterator.next()

            if (indexBelongsToExisting(randomIndex)) {
                val objectTokenId = markingAtPlace.elementAt(randomIndex)
                sortedSet.add(objectTokenId)
            } else {
                val newToken = newTokenGenerationFacade.generateRealToken(objectTypeId)
                newlyGeneratedTokens.add(newToken.id)
                sortedSet.add(newToken.id)
            }
        }
        markingAtPlace.addAll(newlyGeneratedTokens)
        return sortedSet
    }

    private fun createRandomizerOrNoOp(): Randomizer {
        return random?.let {
            ByRandomRandomizer(random)
        } ?:  NoOpIteratorRandomizer()
    }

    override fun shuffleTokens(tokens: List<ObjectTokenId>): List<ObjectTokenId> {
        if (random == null) return repeatabilityInteractor.sortTokens(tokens)
        return repeatabilityInteractor.sortTokens(tokens).shuffled(random)
    }

    override fun shuffleTokens(tokens: SortedSet<ObjectTokenId>): SortedSet<ObjectTokenId> {
        if (random == null) return tokens

        return tokens.shuffled(random).toSortedSet()
    }

    companion object {
        const val GUARD_MULTIPLIER = 10
    }
}
