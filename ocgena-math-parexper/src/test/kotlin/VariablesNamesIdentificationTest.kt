import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.expression.facade.fullConvertM
import ru.misterpotz.expression.facade.getVariablesNames

class VariablesNamesIdentificationTest {

    @Test
    fun singleVariableNameIdentified() {
        val expr = "n"
        val mathNode = expr.fullConvertM

        Assertions.assertEquals(1, mathNode.getVariablesNames().size)
    }

    @Test
    fun singleVariableExprNameIdentified() {
        val expr = "6*n"
        val mathNode = expr.fullConvertM

        Assertions.assertEquals(1, mathNode.getVariablesNames().size)
    }

    @Test
    fun variablesNamesIdentified() {
        val expr = "kek * lol - arbidol + 5"
        val mathNode = expr.fullConvertM
        val variablesNames = mathNode.getVariablesNames()
        Assertions.assertEquals(3, variablesNames.size)
        Assertions.assertEquals(setOf("kek", "lol", "arbidol"), variablesNames.toSet())
    }

    @Test
    fun noVariablesYieldsNoResults() {
        val expr = "5 - 34 * (-4) + 34-10".fullConvertM
        val variablesNamse = expr.getVariablesNames()
        Assertions.assertTrue(variablesNamse.isEmpty())
    }
}