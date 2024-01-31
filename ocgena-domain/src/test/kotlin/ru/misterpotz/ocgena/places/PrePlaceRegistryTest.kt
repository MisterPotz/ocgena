package ru.misterpotz.ocgena.places

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import ru.misterpotz.ocgena.DEFAULT_SETTINGS
import ru.misterpotz.ocgena.ModelPath
import ru.misterpotz.ocgena.addTokens
import ru.misterpotz.ocgena.readAndBuildConfig
import ru.misterpotz.ocgena.simComponentOld
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import java.util.stream.Stream

class PrePlaceRegistryTest {

    @Test
    fun transitionPrePlaceContainEnoughtTokens() {
        val config = readAndBuildConfig(DEFAULT_SETTINGS, ModelPath.THREE_IN_TWO_OUT)
        val simComp = simComponentOld(config).addTokens {
            forPlace("p1", 4)
            forPlace("o1", 1)
            forPlace("p3", 2)
        }

        with(simComp) {
            val arcPrePlaceHasEnoughTokensChecker = arcPrePlaceHasEnoughTokensChecker()
            val tokenAmountStorage = objectTokenRealAmountRegistry()

            Assertions.assertTrue(
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens("p1", "t1", tokenAmountStorage)
            )
            Assertions.assertTrue(
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens("o1", "t1", tokenAmountStorage)
            )
            Assertions.assertFalse(
                arcPrePlaceHasEnoughTokensChecker.arcInputPlaceHasEnoughTokens("p3", "t1", tokenAmountStorage)
            )
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ArgsProvider::class)
    fun transitionPrePlaceContainEnoughTokens(testArgs: TestArgs) = with(testArgs.simulationComponent) {


        Assertions.assertEquals(
            testArgs.notEnoughTokens,
            prePostPlaceRegistry().transitionPrePlaces("t1") > objectTokenRealAmountRegistry()
        )
    }

    companion object {
        class TestArgs(
            val simulationComponent: SimulationComponent,
            val notEnoughTokens: Boolean,
        )

        class ArgsProvider() : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {


                return Stream.of(
                    Arguments.of(
                        run {
                            val config = readAndBuildConfig(DEFAULT_SETTINGS, ModelPath.THREE_IN_TWO_OUT)
                            val simComp = simComponentOld(config).addTokens {
                                forPlace("p1", 4)
                                forPlace("o1", 1)
                                forPlace("p3", 2)
                            }

                            TestArgs(
                                simComp,
                                notEnoughTokens = true
                            )
                        }
                    ),
                    Arguments.of(
                        run {
                            val config = readAndBuildConfig(DEFAULT_SETTINGS, ModelPath.THREE_IN_TWO_OUT)
                            val simComp = simComponentOld(config).addTokens {
                                forPlace("p1", 0)
                                forPlace("o1", 1)
                                forPlace("p3", 3)
                            }

                            TestArgs(
                                simComp,
                                notEnoughTokens = true
                            )
                        }
                    ),
                    Arguments.of(
                        run {
                            val config = readAndBuildConfig(DEFAULT_SETTINGS, ModelPath.THREE_IN_TWO_OUT)
                            val simComp = simComponentOld(config).addTokens {
                                forPlace("p1", 1)
                                forPlace("o1", 1)
                                forPlace("p3", 3)
                            }

                            TestArgs(
                                simComp,
                                notEnoughTokens = false
                            )
                        }
                    )
                )
            }

        }
    }
}