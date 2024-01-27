package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.createPartiallyPredefinedSeq
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.utils.makeObjTypeId
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractorImpl
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import java.util.stream.Stream
import kotlin.random.Random

internal class TokenSelectionInteractorTest {

    val otype1 = "1".makeObjTypeId()
    val otype2 = "2".makeObjTypeId()

    val p1 = "p1"
    val p2 = "p2"
    val p3 = "p3"
    val p4 = "p4"

    val ebobus = SparseTokenBunchImpl.makeBuilder {
        forPlace(p1) {
            type = otype1
            realTokens = 6
            initializedTokens.addAll(listOf(11, 12, 13))
        }
        forPlace(p2) {
            type = otype2
            realTokens = 10
        }
        forPlace(p3) {
            realTokens = 1
            initializedTokens.add(31)
        }
        forPlace(p4) {
            realTokens = 4
            initializedTokens.addAll(listOf(41, 42, 43, 44))
        }
    }.buildWithTypeRegistry()
    val tokenBunch = ebobus.first
    val placeToType = ebobus.second

    private fun createmockkTokenGenFacade(): NewTokenGenerationFacade {
        return mockk<NewTokenGenerationFacade> {
            var id = 100L
            every { generateRealToken(any()) } answers {
                val otype = firstArg<String>()
                createMockkToken(++id, type = otype)
            }
        }
    }

    @Test
    fun `token selection interactor fails when more tokens requested than it can allow`() {
        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = mockk(),
            newTokenGenerationFacade = createmockkTokenGenFacade(),
            placeToObjectTypeRegistry = placeToType
        )

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
                petriAtomId = p3,
                amount = 2,
                tokenBunch
            )
        }
    }


    @ParameterizedTest
    @ArgumentsSource(FairlyInitializesAndSelectsProvider::class)
    fun `token selects not only existing but initializes other items with random as well even if number to initialize is smaller than existing`(
        seq: List<Int>
    ) {
        println(seq)
        val randomMokk = createPartiallyPredefinedSeq(seq)
        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = randomMokk,
            newTokenGenerationFacade = createmockkTokenGenFacade(),
            placeToObjectTypeRegistry = placeToType
        )

        val result = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = p1,
            amount = 3,
            tokenBunch
        )

        Assertions.assertTrue(result.selected.intersect(tokenBunch.objectMarking()[p1]).isNotEmpty())
        Assertions.assertTrue(result.generated.intersect(tokenBunch.objectMarking()[p1]).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(NotEnoughTokensInitializationProvider::class)
    fun `token initializes all the required items if there are not enough tokens`(seq: List<Int>) {
        val randomMokk = createPartiallyPredefinedSeq(seq)
        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = randomMokk,
            newTokenGenerationFacade = createmockkTokenGenFacade(),
            placeToObjectTypeRegistry = placeToType
        )

        val result = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = p1,
            amount = seq.size,
            tokenBunch
        )

        Assertions.assertTrue(result.selected != tokenBunch.objectMarking()[p1])
        Assertions.assertTrue(result.selected.intersect(tokenBunch.objectMarking()[p1]).isNotEmpty())
        Assertions.assertTrue(result.generated.intersect(tokenBunch.objectMarking()[p1]).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(CanInitializeAllProvider::class)
    fun `initializes all tokens completely if there exists no tokens yet`(seq: List<Int>) {
        val randomMokk = createPartiallyPredefinedSeq(seq)

        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = randomMokk,
            newTokenGenerationFacade = createmockkTokenGenFacade(),
            placeToObjectTypeRegistry = placeToType
        )

        val result = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = p2,
            amount = seq.size,
            tokenBunch
        )

        Assertions.assertTrue(result.selected.size == seq.size)
        Assertions.assertTrue(result.generated.size == seq.size)
        Assertions.assertTrue(result.selected != tokenBunch.objectMarking()[p1])
        Assertions.assertTrue(result.selected.intersect(tokenBunch.objectMarking()[p1]).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(WorksOkWithRepeatedItemsProvider::class)
    fun `works correctly when random returns duplicate items`(
        repetitionsTestData: RepetitionsTestData
    ) {
        val randomMOckk = createPartiallyPredefinedSeq(repetitionsTestData.predefinedSeq)
        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = randomMOckk,
            newTokenGenerationFacade = createmockkTokenGenFacade(),
            placeToObjectTypeRegistry = placeToType,
        )

        val result = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = repetitionsTestData.place,
            amount = repetitionsTestData.toSelect,
            tokenBunch = tokenBunch
        )

        Assertions.assertTrue(result.selected.size == repetitionsTestData.toSelect)
    }


    private fun createMockkToken(
        id: ObjectTokenId,
        type: String
    ): ObjectToken {
        return ObjectToken(
            id = id,
            name = "mokk_$id",
            objectTypeId = type,
        )
    }

    companion object {

        fun <PerTest> createArgProvider(list: List<PerTest>): ArgumentsProvider {
            return ArgumentsProvider {
                val arrayOfArgs: Array<Arguments> = list.map { Arguments.of(it) }.toTypedArray()
                Stream.of(*arrayOfArgs)
            }
        }


        private class NotEnoughTokensInitializationProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                (0..5).shuffled(),
                (0..4).shuffled(),
                (0..3).shuffled(),
                (0..5).shuffled(),
                (0..4).shuffled(),
            )
        )


        fun take1Existing2Unexisting(): List<Int> {
            // take 1 from existing
            // take 2 from unexisting
            return buildList {
                add((0..2).random())
                addAll((3..5).shuffled().subList(0, 2))
            }
                .shuffled()
        }

        private class FairlyInitializesAndSelectsProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                listOf(0, 5, 3),
                take1Existing2Unexisting(),
                take1Existing2Unexisting(),
                take1Existing2Unexisting()
            )
        )

        private class CanInitializeAllProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                (0..9).shuffled(),
                (0..(Random.nextInt(0, 10))).shuffled(),
                (0..(Random.nextInt(0, 10))).shuffled(),
                (0..(Random.nextInt(0, 10))).shuffled(),
            )
        )

        data class RepetitionsTestData(
            val place: PetriAtomId,
            val predefinedSeq: List<Int>,
            val toSelect: Int,
        )

        private class WorksOkWithRepeatedItemsProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                RepetitionsTestData(
                    place = "p1",
                    predefinedSeq = listOf(3, 5, 3, 5, 3, 5, 1, 3),
                    toSelect = 5
                ),
                RepetitionsTestData(
                    place = "p2",
                    predefinedSeq = listOf(3, 5, 3, 5, 3, 5, 1, 3, 9, 9, 7, 6),
                    toSelect = 8
                ),
                RepetitionsTestData(
                    place = "p3",
                    predefinedSeq = listOf(0, 0, 0, 0, 0, 0, 0, 0),
                    toSelect = 1
                ),
                RepetitionsTestData(
                    place = "p4",
                    predefinedSeq = listOf(1, 3, 3, 3, 3, 3, 3, 1, 1),
                    toSelect = 4
                ),
            )
        )
    }
}