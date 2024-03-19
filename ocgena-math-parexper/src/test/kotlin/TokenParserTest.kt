import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.expression.facade.fullConvertM
import ru.misterpotz.expression.paramspace.VariableParameterSpace

class TokenParserTest {

    @Test
    fun stringExpressionIsParsedAndEvaluated() {
        val expression = "-10*4+250*(-1)-(-2*20+4)".fullConvertM
        val expected = -254.0

        Assertions.assertEquals(expected, expression.evaluate())
    }

    @Test
    fun expressionWithVariableIsEvaluated() {
        val expression = "5+(-  kek ) *34  - 3".fullConvertM
        Assertions.assertEquals(
            -100.0,
            expression.evaluate(
                parameterSpace = VariableParameterSpace(
                    "kek" to 3.0
                )
            )
        )
    }

    @Test
    fun doubleNumbersWorkOk() {
        val expression = "(5.5 +(-  kek ) *34  - 3) * 0.5".fullConvertM
        Assertions.assertEquals(
            -49.75,
            expression.evaluate(
                parameterSpace = VariableParameterSpace(
                    "kek" to 3.0
                )
            )
        )
    }

    @Test
    fun nestedBracksWorkOk() {
        val expression = "((((((-4) + 4) * 3 - 20)*2)-4))".fullConvertM
        Assertions.assertEquals(-44.0, expression.evaluate())
    }

    @Test
    fun throwsIfNoValueForVariablePassed() {
        val expression = "5+(-  kek ) *34  - 3".fullConvertM

        Assertions.assertThrows(NullPointerException::class.java) {
            expression.evaluate(VariableParameterSpace())
        }
    }
}