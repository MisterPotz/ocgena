package ru.misterpotz.ocgena.tokens

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.createArgProvider
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl

class SimpleSparseTokenBunchTest {
    @ParameterizedTest
    @ArgumentsSource(ConcatAndDeductCases::class)
    fun `token bunch performs operation as expected`(testData: TestData) {
        val sparseTokenBunch = testData.starterBunch

        if (testData.operationSign > 0) {
            sparseTokenBunch.append(testData.appliedBunch)
        } else {
            sparseTokenBunch.minus(testData.appliedBunch)
        }
        Assertions.assertTrue(sparseTokenBunch.bunchesEqual(testData.expectedBunch))
    }

    @ParameterizedTest
    @ArgumentsSource(DeductMoreThanAbleCase::class)
    fun `fails when tries to deduct more than able according to token amount storage`(testData: TestData) {
        val sparseTokenBunch = testData.starterBunch
        Assertions.assertThrows(IllegalStateException::class.java) {
            sparseTokenBunch.minus(testData.appliedBunch)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(DeductForInvalidState::class)
    fun `fails when after deduction there are more tokens in the marking than according to token amount storage`(testData: TestData) {
        val sparseTokenBunch = testData.starterBunch
        Assertions.assertThrows(IllegalStateException::class.java) {
            sparseTokenBunch.minus(testData.appliedBunch)
        }
    }

    companion object {

        data class TestData(
            val starterBunch : SparseTokenBunch,
            val appliedBunch: SparseTokenBunch,
            val operationSign: Int,
            val expectedBunch: SparseTokenBunch
        )

        fun emptyConcatSome(): TestData {
            return TestData(
                starterBunch = SparseTokenBunchImpl.makeBuilder {  }.buildTokenBunch(),
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
            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
                forPlace("p2") {
                    addAll(21)
                }
            }.buildTokenBunch()

            return TestData(
                starterBunch = starterBunch,
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
            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
                forPlace("p2") {
                    addAll(21)
                    realTokens = 6
                }
            }.buildTokenBunch()

            return TestData(
                starterBunch = starterBunch,
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
            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
                forPlace("p2") {
                    addAll(21)
                    realTokens = 6
                }
                forPlace("p3") {
                    addAll(31, 32)
                    realTokens = 2
                }
                forPlace("p4") {
                    addAll(41, 42)
                    realTokens = 7
                }
            }.buildTokenBunch()

            return TestData(
                starterBunch = starterBunch,
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

            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
                forPlace("p2") {
                    addAll(21)
                    realTokens = 6
                }
            }.buildTokenBunch()


            return TestData(
                starterBunch = starterBunch,
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
            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
            }.buildTokenBunch()

            return TestData(
                starterBunch = starterBunch,
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
            val starterBunch = SparseTokenBunchImpl.makeBuilder {
                forPlace("p1") {
                    addAll(1, 2, 3)
                    realTokens = 8
                }
            }.buildTokenBunch()

            return TestData(
                starterBunch = starterBunch,
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