package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.utils.ByRandomRandomizer
import ru.misterpotz.ocgena.utils.NoOpIteratorRandomizer
import ru.misterpotz.ocgena.utils.RandomIterator
import ru.misterpotz.ocgena.utils.Randomizer
import simulation.random.RandomSource
import java.util.*
import javax.inject.Inject
import javax.inject.Qualifier
import kotlin.random.Random

interface TokenSelectionInteractor {
    fun selectAndInitializeTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int,
        tokenBunch: SparseTokenBunch,
    ): SelectedAndGeneratedTokens

    fun selectTokensFromBuffer(
        tokenSet: TokenSet,
        selectionAmount: Int,
    ): SortedSet<ObjectTokenId>

    fun generateTokens(objectTypeId: ObjectTypeId, amount: Int): SortedSet<ObjectTokenId>

    class SelectedAndGeneratedTokens(
        val selected: SortedSet<ObjectTokenId>,
        val generated: SortedSet<ObjectTokenId>,
    )
}
class TokenSelectionInteractorImpl @Inject constructor(
    private val randomSource: RandomSource,
    private val newTokenGenerationFacade: NewTokenGenerationFacade,
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
) : TokenSelectionInteractor {

//    val pMarking get() = pMarkingProvider.get()

    override fun selectAndInitializeTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int,
        tokenBunch: SparseTokenBunch,
    ): TokenSelectionInteractor.SelectedAndGeneratedTokens {
        val markingAtPlace = tokenBunch.objectMarking()[petriAtomId]
        val tokensAtPlace = tokenBunch.tokenAmountStorage().getTokensAt(petriAtomId)
        val existing = markingAtPlace.size
        fun indexBelongsToExisting(index: Int): Boolean {
            return index < existing
        }

        val sortedSet = sortedSetOf<ObjectTokenId>()
        val randomizer = createRandomizerOrNoOp()
        val objectTypeId = placeToObjectTypeRegistry[petriAtomId]
        val randomIterator = RandomIterator(
            size = amount,
            randomizer = randomizer,
            rangeToSelectFrom = 0 until tokensAtPlace
        )

        require(tokensAtPlace >= amount) {
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
        return TokenSelectionInteractor.SelectedAndGeneratedTokens(
            selected = sortedSet,
            generated = newlyGeneratedTokens
        )
    }

    override fun selectTokensFromBuffer(
        tokenSet: TokenSet,
        selectionAmount: Int,
    ): SortedSet<ObjectTokenId> {
        if (selectionAmount >= tokenSet.size) {
            return tokenSet.toSortedSet()
        }
        val targetSet = sortedSetOf<ObjectTokenId>()

        val randomizer = createRandomizerOrNoOp()

        val randomIterator = RandomIterator(
            size = selectionAmount.coerceAtMost(tokenSet.size),
            randomizer = randomizer,
            rangeToSelectFrom = 0 until tokenSet.size
        )

        while (randomIterator.hasNext()) {
            val randomIndex = randomIterator.next()
            val objectTokenId = tokenSet.elementAt(randomIndex)
            targetSet.add(objectTokenId)
        }

        return targetSet
    }

    override fun generateTokens(objectTypeId: ObjectTypeId, amount: Int): SortedSet<ObjectTokenId> {
        val targetSet = sortedSetOf<ObjectTokenId>()

        for (i in 0 until amount) {
            val newToken = newTokenGenerationFacade.generateRealToken(objectTypeId)
            targetSet.add(newToken.id)
        }
        return targetSet
    }

    private fun createRandomizerOrNoOp(): Randomizer {
        return ByRandomRandomizer(randomSource.tokenSelection())
    }

    companion object {
        const val GUARD_MULTIPLIER = 10
    }
}
