package ru.misterpotz.ocgena.tokens

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.createArgProvider
import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistry
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.simulation_old.state.PMarkingProvider
import ru.misterpotz.ocgena.simulation_old.stepexecutor.GlobalSparseTokenBunch
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SparseTokenBunchImpl

class GlobalTokenBunchTest {

    private fun createGlobalTokenBunch(
        placeToObjectMarking: PlaceToObjectMarking,
        objectTokenRealAmountRegistryImpl: ObjectTokenRealAmountRegistry
    ): GlobalSparseTokenBunch {
        val pMarkingProvider = mockk<PMarkingProvider> {
            every { get() } returns placeToObjectMarking
        }

        return GlobalSparseTokenBunch(
            pMarkingProvider,
            objectTokenRealAmountRegistry = objectTokenRealAmountRegistryImpl
        )
    }

    @ParameterizedTest
    @ArgumentsSource(ConcatAndDeductCases::class)
    fun `token bunch performs operation as expected`(testData: TestData) {
        val globalTokenBunch = createGlobalTokenBunch(testData.starter, testData.amountData)

        if (testData.operationSign > 0) {
            globalTokenBunch.append(testData.appliedBunch)
        } else {
            globalTokenBunch.minus(testData.appliedBunch)
        }
        Assertions.assertTrue(globalTokenBunch.bunchesExactlyEqual(testData.expectedBunch))
    }

    @ParameterizedTest
    @ArgumentsSource(DeductMoreThanAbleCase::class)
    fun `fails when tries to deduct more than able according to token amount storage`(testData: TestData) {
        val globalTokenBunch = createGlobalTokenBunch(testData.starter, testData.amountData)
        Assertions.assertThrows(IllegalStateException::class.java) {
            globalTokenBunch.minus(testData.appliedBunch)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(DeductForInvalidState::class)
    fun `fails when after deduction there are more tokens in the marking than according to token amount storage`(testData: TestData) {
        val globalTokenBunch = createGlobalTokenBunch(testData.starter, testData.amountData)
        Assertions.assertThrows(IllegalStateException::class.java) {
            globalTokenBunch.minus(testData.appliedBunch)
        }
    }

    companion object {

        data class TestData(
            val starter: PlaceToObjectMarking,
            val amountData: ObjectTokenRealAmountRegistry,
            val appliedBunch: SparseTokenBunch,
            val operationSign: Int,
            val expectedBunch: SparseTokenBunch
        )

        fun emptyConcatSome(): TestData {
            return TestData(
                starter = ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarkingMap.build {
                },
                amountData = ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenRealAmountRegistryImpl.build {
                },
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 10
                        addAll(1, 4, 5)
                    }
                }.buildTokenBunch(),
                operationSign = 1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 10
                        addAll(1, 4, 5)
                    }
                }.buildTokenBunch()
            )
        }

        private fun someConcatSome(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
                put("p2", setOf(21))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 10
                        addAll(4, 5, 6)
                    }
                }.buildTokenBunch(),
                operationSign = 1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 18
                        addAll(1, 2, 3, 4, 5, 6)
                    }
                    forPlace("p2") {
                        realTokens = 1
                        addAll(21)
                    }
                }.buildTokenBunch()
            )
        }

        private fun someDeductUnexistingSome(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
                put("p2", setOf(21))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
                incrementRealAmountAt("p2", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 6
                        addAll(3, 46)
                    }
                    forPlace("p2") {
                        realTokens = 3
                        addAll(25, 65, 43)
                    }
                }.buildTokenBunch(),
                operationSign = -1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 2
                        addAll(1, 2)
                    }
                    forPlace("p2") {
                        realTokens = 3
                        addAll(21)
                    }
                }.buildTokenBunch()
            )
        }


        private fun someDeductSome(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
                put("p2", setOf(21))
                put("p3", setOf(31, 32))
                put("p4", setOf(41, 42))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
                incrementRealAmountAt("p2", 5)

                incrementRealAmountAt("p4", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 6
                        addAll(3)
                    }
                    forPlace("p2") {
                        realTokens = 3
                    }
                    forPlace("p3") {
                        realTokens = 2
                        addAll(31, 32)
                    }
                    forPlace("p4") {
                        realTokens = 7
                        addAll(41, 42)
                    }
                }.buildTokenBunch(),
                operationSign = -1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 2
                        addAll(1, 2)
                    }
                    forPlace("p2") {
                        realTokens = 3
                        addAll(21)
                    }
                }.buildTokenBunch()
            )
        }

        private fun someDeductTooMuchSome(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
                put("p2", setOf(21))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
                incrementRealAmountAt("p2", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 10
                        addAll(4, 5, 6)
                    }
                }.buildTokenBunch(),
                operationSign = -1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 18
                        addAll(1, 2, 3, 4, 5, 6)
                    }
                    forPlace("p2") {
                        realTokens = 1
                        addAll(21)
                    }
                }.buildTokenBunch()
            )
        }

        private fun someDeductInvalidAmountsEdgeCase(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 8
                    }
                }.buildTokenBunch(),
                operationSign = -1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 0
                    }
                }.buildTokenBunch()
            )
        }

        private fun someDeductInvalidAmounts(): TestData {
            val (tokens, amounts) = PlaceToObjectMarkingMap.buildWithAmount {
                put("p1", setOf(1, 2, 3))
            }
            amounts.apply {
                incrementRealAmountAt("p1", 5)
            }

            return TestData(
                starter = tokens,
                amountData = amounts,
                appliedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 7
                    }
                }.buildTokenBunch(),
                operationSign = -1,
                expectedBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p1") {
                        realTokens = 0
                    }
                }.buildTokenBunch()
            )
        }

        private class ConcatAndDeductCases : ArgumentsProvider by createArgProvider(
            listOf(
                emptyConcatSome(),
                someConcatSome(),
                someDeductUnexistingSome(),
                someDeductSome()
            )
        )

        private class DeductMoreThanAbleCase : ArgumentsProvider by createArgProvider(
            listOf(
                someDeductTooMuchSome()
            )
        )

        private class DeductForInvalidState : ArgumentsProvider by createArgProvider(
            listOf(
                someDeductInvalidAmountsEdgeCase(),
                someDeductInvalidAmounts()
            )
        )
    }
}