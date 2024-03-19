import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.expression.paramspace.EmptyParameterSpace
import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.expression.m
import ru.misterpotz.expression.plus
import ru.misterpotz.expression.times

class VariableTest {
    @Test
    fun simpleCalculation() {
        val expr = 5.m + "k".m * 2.m

        val value = expr.evaluate(VariableParameterSpace("k" to 3.0))

        Assertions.assertEquals(11.0, value)
    }

    @Test
    fun throws() {
        val expr = 5.m + "k".m * 2.m

        Assertions.assertThrows(NotImplementedError::class.java) {
            expr.evaluate(EmptyParameterSpace)
        }
    }
}