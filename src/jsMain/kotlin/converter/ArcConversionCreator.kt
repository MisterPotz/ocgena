package converter

import ast.*
import model.Arc
import model.NormalArc
import model.typea.VariableArcTypeA
import model.typel.Expression
import model.typel.VariableArcTypeL
import model.utils.ElementsIdCreator

class ArcConversionCreator(private val conversionEntitiesCreator: ConversionEntitiesCreator) {
    val elementsIdCreator = ElementsIdCreator()


    fun retrieveExpressionFromParams(edgeOpParams: EdgeOpParams): Expression {
        if (edgeOpParams.type == OpParamsTypes.Number) {
            return object : Expression(variable = null, stringExpr = null) {
                override fun substitute(variableValue: Int): Int {
                    return edgeOpParams.asDynamic().value
                }
            }
        } else {
            val stringifiedExpression = Compiler().stringifyExpression(expression = edgeOpParams as RootExpression)
            val expressionExecutor = ExpressionExecutor(edgeOpParams as RootExpression)
            val variable = expressionExecutor.variable

            return object : Expression(
                variable = variable,
                stringExpr = stringifiedExpression
            ) {
                override fun substitute(variableValue: Int): Int {
                    return expressionExecutor.substitute(variableValue)
                }
            }
        }
    }


    private fun createVariableArc(fromNodeId: String, toNodeId: String, edgeOpParams: EdgeOpParams?): Arc {
        val fromNode = conversionEntitiesCreator.elementByLabel(fromNodeId)
        val toNode = conversionEntitiesCreator.elementByLabel(toNodeId)
        val id = elementsIdCreator.createArcFromIds(fromNodeId, toNodeId)

        return if (edgeOpParams == null) {
            VariableArcTypeA(id = id, arrowNode = toNode, tailNode = fromNode)
        } else {
            val expression = retrieveExpressionFromParams(edgeOpParams)

            VariableArcTypeL(
                id = id,
                arrowNode = toNode,
                tailNode = fromNode,
                expression = expression
            )
        }
    }

    fun createArc(fromNodeId: String, toNodeId: String, edgeRHSElement: EdgeRHSElement): Arc {
        val opType = edgeRHSElement.edgeop.type

        return if (opType == "=>") {
            createVariableArc(fromNodeId, toNodeId, edgeRHSElement.edgeop.params)
        } else {
            createNormalArc(fromNodeId, toNodeId, edgeRHSElement.edgeop.params)
        }
    }

    private fun createNormalArc(fromNodeId: String, toNodeId: String, params: EdgeOpParams?): Arc {
        val fromNode = conversionEntitiesCreator.elementByLabel(fromNodeId)
        val toNode = conversionEntitiesCreator.elementByLabel(toNodeId)

        val multiplicity = if (params?.type == OpParamsTypes.Number) {
            (params as OpParamsNumber).value.toInt()
        } else {
            1
        }
        val id = elementsIdCreator.createArcFromIds(fromNodeId, toNodeId)

        return NormalArc(
            id = id,
            arrowNode = toNode,
            tailNode = fromNode,
            multiplicity = multiplicity
        )
    }
}
