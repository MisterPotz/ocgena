package ru.misterpotz.ocgena.dsl.simulation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.tool.readFolderConfig
import ru.misterpotz.ocgena.simulation.config.SettingsSimulationConfig
import ru.misterpotz.ocgena.utils.findInstance

annotation class TestFolder(val folderName: String)

@TestFolder(folderName = "exec_continuation")
class ExecutionContinuationTest {


    @Test
    fun config() {
        val settingsConfig : SettingsSimulationConfig = readFolderConfig("settings.yaml")
    }
    @Test
    fun stopsWhenMarkingHasFiveTokens() = runTest {
//        Assertions.assertNotNull(settingsConfig)
    }
}

inline fun <reified T, R : Any> T.withFolderName(action: (String) -> R): R {
    val folderName = this!!::class.annotations.findInstance<TestFolder>()!!.folderName
    return action(folderName)
}

inline fun <reified T, reified R : Any> T.readFolderConfig(name: String): R {
    return withFolderName {
        readFolderConfig<R>(it, name)
    }
}

