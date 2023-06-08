import ast.RootExpression
import converter.ExpressionExecutor
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionExecutorTest {
    @Test
    fun expressionEvaluation() {
        // (3*k + 1)
        val rootExpressionString = """{"type":"expression","head":{"head":3,"tail":[{"op":"*","target":{"variable":"k"}}]},"tail":[{"op":"+","target":{"head":1,"tail":[]}}],"location":{"start":{"offset":0,"line":1,"column":1},"end":{"offset":9,"line":1,"column":10}}}"""

        val rootExpression = JSON.parse<dynamic>(rootExpressionString)

        val expressionExecutor = ExpressionExecutor(rootExpression as RootExpression)

        expressionExecutor.variable
    }
}
