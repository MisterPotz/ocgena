package ru.misterpotz.ocgena.dsl.randomization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.*
import ru.misterpotz.ocgena.simulation.config.SettingsSimulationConfig
import ru.misterpotz.ocgena.simulation.config.SimulationConfig

class TokenSelectorTest {

    fun createSimComponent(): FacadeSim {
        val model = ModelPath.ONE_IN_TWO_OUT.load()
        val config = readConfig<SettingsSimulationConfig>(DEFAULT_SETTINGS)

        val simcomp = simComponent(
            SimulationConfig.fromNetAndSettings(model, config)
                .copy(
                    randomSeed = null
                )
        )

        return simcomp.facade()
    }

    @Test
    fun `when no random doesn't select randomly`() {
        val (component, task, config) = createSimComponent()

        component.withTokens {
            addOfType("o1", 1, 2, 3, 4, 5, 6)
        }
        val tokenSelector = component.tokenSelectionInteractor()
    }
}