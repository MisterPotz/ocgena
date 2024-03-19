package ru.misterpotz.ocgena.timepn

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.serialization.ModelYamlCreator
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunchImpl
import ru.misterpotz.ocgena.testing.*

class TransitionFiringRuleExecutorTest {

    @ParameterizedTest
    @ArgumentsSource(T3TransitionCasesProvider::class)
    fun `transition firing rule yields expected results`(testConfig: TestConfig) {
        val simComp = buildConfig {
            ocNetType = testConfig.ocNetType
            ocNetStruct = testConfig.model
            semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
        }.toSimComponent()
            .addBunch(testConfig.initialBunch)

        val transitionFiringRuleExecutor = simComp.transitionFiringRuleExecutor()
        val transition = simComp.transition("t3")

        simComp.beforeNewStep()
        transitionFiringRuleExecutor.fireTransition(transition)

        Assertions.assertTrue(
            simComp.tokenBunch()
                .narrowTo(transition.neighbourhood())
                .projectBunchAmountsEqual(
                    testConfig.expectedEndLocalBunch
                )
        )
    }

    companion object {
        data class TestConfig(
            val model: OCNetStruct,
            val ocNetType: OcNetType,
            val initialBunch: SparseTokenBunch,
            val expectedEndLocalBunch: SparseTokenBunch,
        )

        fun firingVariableArcLomazova(): TestConfig {
            return TestConfig(
                initialBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("o1") {
                        realTokens = 4
                    }
                    forPlace("p2") {
                        realTokens = 10
                    }
                }.buildTokenBunch(),
                model = buildOCNet {
                    buildingBlockTwoInTwoOutMiddle().installOnto(this)
                },
                expectedEndLocalBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p2") {
                        realTokens = 9
                    }
                    forPlace("o1") {
                        realTokens = 0
                    }
                    forPlace("o2") {
                        realTokens = 4
                    }
                }.buildTokenBunch(),
                ocNetType = OcNetType.LOMAZOVA
            )
        }

        fun firingVariableMathArcLomazova(): TestConfig {
            return TestConfig(
                initialBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("o1") {
                        realTokens = 4
                    }
                    forPlace("p2") {
                        realTokens = 10
                    }
                }.buildTokenBunch(),
                model = buildOCNet {
                    buildingBlockTwoInTwoOutMiddle().installOnto(this)

                    "o1".arc("t3".t) { vari; mathExpr = "n" }
                        .arc("o2".p { objectTypeId = "2"; output }) { vari; mathExpr = "6*n" }

                    "t3".arc("p3") { norm; multiplicity = 5 }
                },
                expectedEndLocalBunch = SparseTokenBunchImpl.makeBuilder {
                    forPlace("p2") {
                        realTokens = 9
                    }
                    forPlace("o1") {
                        realTokens = 0
                    }
                    forPlace("o2") {
                        realTokens = 24
                    }
                    forPlace("p3") {
                        realTokens = 5
                    }
                }.buildTokenBunch(),
                ocNetType = OcNetType.LOMAZOVA
            )
        }

        class T3TransitionCasesProvider : ArgumentsProvider by createArgProvider(
            list = listOf(
                firingVariableArcLomazova(),
                firingVariableMathArcLomazova()
            )
        )
    }
}