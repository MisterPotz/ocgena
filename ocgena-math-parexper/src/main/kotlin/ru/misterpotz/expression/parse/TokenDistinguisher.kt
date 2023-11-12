package ru.misterpotz.expression.parse

import ru.misterpotz.expression.NodeSpace
import ru.misterpotz.expression.builder.TokenParseBuilder
import ru.misterpotz.expression.processedOpSet

class TokenDistinguisher(private val nodeSpace: NodeSpace) {
    fun createOpsNodes(tokens: List<String>): TokenParseBuilder.OpsNodes {
        val ops = mutableListOf<String>()
        val valueAndNodes = mutableListOf<String>()

        for (token in tokens) {
            if (token in processedOpSet) {
                ops.add(token)
            } else {
                valueAndNodes.add(token)
            }
        }

        return TokenParseBuilder.OpsNodes(
            ops = ops,
            valueAndNodes = valueAndNodes,
            nodeSpace = nodeSpace
        )
    }
}