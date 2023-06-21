package utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class InfixExpressionTreeCreatorTest {
    @Test
    fun parseExpressionWithUnary() {
        // 4 + ( - 5 * 10 )
        val expression = listOf("4", "+", "(", "-", "5", "*", "10", ")")
        val infixExpressionTreeCreator = InfixExpressionTreeCreator()

        val tree = infixExpressionTreeCreator.infixExpression(expression)
        assertEquals("(4+((-5)*10))", tree.print())
    }

    @Test
    fun parseExpressionPrecedence() {
        val expression = listOf("4", "+", "5", "*", "2", "-", "10")
        val infixExpressionTreeCreator = InfixExpressionTreeCreator()

        val tree = infixExpressionTreeCreator.infixExpression(expression)
        assertEquals("((4+(5*2))-10)", tree.print())
    }

    @Test
    fun withVariable() {
        val expression = listOf("4", "+", "5", "*", "k", "-", "10", "-", "3", "*", "k")
        val infixExpressionTreeCreator = InfixExpressionTreeCreator()
        val tree = infixExpressionTreeCreator.infixExpression(expression)
        assertEquals(tree.print(), "(((4+(5*k))-10)-(3*k))")
    }

}
