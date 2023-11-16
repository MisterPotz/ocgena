package ru.misterpotz.ocgena.simulation.interactors

import ru.misterpotz.ocgena.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenBuffer
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

    class SelectedAndGeneratedTokens(
        val selected: SortedSet<ObjectTokenId>,
        val initialized: SortedSet<ObjectTokenId>
    )

    fun selectAndInitializeTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int
    ): SelectedAndGeneratedTokens

    fun selectAndRemoveTokensFromBuffer(
        tokenBuffer: TokenBuffer,
        selectionAmount: Int
    ): SortedSet<ObjectTokenId>

    fun generateTokens(objectTypeId: ObjectTypeId, amount: Int): SortedSet<ObjectTokenId>

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

    override fun selectAndInitializeTokensFromPlace(
        petriAtomId: PetriAtomId,
        amount: Int
    ): TokenSelectionInteractor.SelectedAndGeneratedTokens {
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
        return TokenSelectionInteractor.SelectedAndGeneratedTokens(
            selected = sortedSet,
            initialized = newlyGeneratedTokens
        )
    }

    override fun selectAndRemoveTokensFromBuffer(
        tokenBuffer: TokenBuffer,
        selectionAmount: Int
    ): SortedSet<ObjectTokenId> {
        val bufferSize = tokenBuffer.size
        val targetSet = sortedSetOf<ObjectTokenId>()

        val randomizer = createRandomizerOrNoOp()
        val randomIterator = RandomIterator(
            amount = selectionAmount,
            randomizer = randomizer
        )

        while (randomIterator.hasNext()) {
            val randomIndex = randomIterator.next()
            val objectTokenId = tokenBuffer.elementAt(randomIndex)
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
        return random?.let {
            ByRandomRandomizer(random)
        } ?: NoOpIteratorRandomizer()
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
