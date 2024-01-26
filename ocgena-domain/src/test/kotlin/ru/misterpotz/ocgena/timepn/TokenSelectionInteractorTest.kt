package ru.misterpotz.ocgena.timepn

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractorImpl
import java.util.stream.Stream

internal class TokenSelectionInteractorTest {

    @Test
    fun `token selection interactor fails when more tokens requested than it can allow`() {

        val newTokenGenerationFacade = mockk<NewTokenGenerationFacade> {
            every { generateRealToken(any()) } returns mockk<ObjectToken>()
        }

        val tokenSelectionInteractor = TokenSelectionInteractorImpl(
            random = null,
            newTokenGenerationFacade =
        )
    }

    companion object {

        class TestData(
            val data,
            val result,
        ) : Arguments

        private class ArgProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
                return Stream.of(
                    Arguments.of(

                    ),
                    Arguments.of(

                    )
                )
            }

        }
    }
}