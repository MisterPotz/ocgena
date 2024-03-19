import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.expression.paramspace.EmptyParameterSpace
import ru.misterpotz.expression.NoBrackExprParser
import ru.misterpotz.expression.NodeSpace
import ru.misterpotz.expression.builder.TokenParseBuilder
import ru.misterpotz.expression.minus
import ru.misterpotz.expression.plus
import ru.misterpotz.expression.product
import ru.misterpotz.expression.unminus

class NoBrackParserTest {

    @Test
    fun simpleExpTest() {
        val nodeSpace = NodeSpace()
        val noBrackExprParser = NoBrackExprParser(
            opsNodes = TokenParseBuilder.OpsNodes(
                ops = listOf("+", unminus, "*"),
                valueAndNodes = listOf("1", "3", "5"),
                nodeSpace = nodeSpace
            )
        )
        val resultingNode = noBrackExprParser.fullTransform()

        Assertions.assertEquals(-14.0, resultingNode.evaluate())
    }

    @Test
    fun valueIndexForOpsTest() {
        val nodeSpace = NodeSpace()
        val noBrackExprParser = NoBrackExprParser(
            opsNodes = TokenParseBuilder.OpsNodes(
                ops = listOf(unminus, "+", unminus, "*"),
                valueAndNodes = listOf("3", "4", "5"),
                nodeSpace = nodeSpace
            )
        )

        Assertions.assertEquals(0, noBrackExprParser.getValuesPointersForOp(0)!!.requireSingle)
        Assertions.assertEquals(0..1, noBrackExprParser.getValuesPointersForOp(1)!!.requireRange)
        Assertions.assertEquals(1, noBrackExprParser.getValuesPointersForOp(2)!!.requireSingle)
        Assertions.assertEquals(1..2, noBrackExprParser.getValuesPointersForOp(3)!!.requireRange)
    }

    @Test
    fun moreReductionTest() {
        val nodeSpace = NodeSpace()
        val noBrackExprParser = NoBrackExprParser(
            opsNodes = TokenParseBuilder.OpsNodes(
                ops = listOf(unminus, "+", unminus, "*"),
                valueAndNodes = listOf("3", "4", "5"),
                nodeSpace = nodeSpace
            )
        )

        val node = noBrackExprParser.fullTransform()
        val evaluation = node.evaluate()
        println(noBrackExprParser.opStack)
        println(noBrackExprParser.valueAndNodes)
        println(nodeSpace)
        Assertions.assertEquals(-23.0, evaluation)
    }

    @Test
    fun complexExpTest() {
        val nodeSpace = NodeSpace()
        val noBrackExprParser = NoBrackExprParser(
            opsNodes = TokenParseBuilder.OpsNodes(
                ops = listOf(unminus, product, minus, unminus, plus, minus, product),
                valueAndNodes = listOf("10", "5", "3", "20", "1", "2"),
                nodeSpace
            )
        )
        val expr = noBrackExprParser.fullTransform()

        val evaluation = expr.evaluate(EmptyParameterSpace)
        Assertions.assertEquals(-29.0, evaluation)
    }
}