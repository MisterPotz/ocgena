package ru.misterpotz.expression.builder

import ru.misterpotz.expression.*
import ru.misterpotz.expression.error.UnrecognizedTokenError
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.expression.parse.TokenDistinguisher

class TokenParseBuilder(tokens: List<String>) {
    private val tokens = mutableListOf<String>().apply { addAll(tokens) }
    private val nodeSpace = NodeSpace()
    private val tokenDistinguisher = TokenDistinguisher(nodeSpace)
    private val brackStack = mutableListOf<String>()

    fun buildExpression(): MathNode {
        validateTokens()
        replaceTokensWhereNeeded()
        reduceAllBracks()
        return finalReduce()
    }

    private fun convertTokenIfNecessary(token: String, lastToken: String?): String {
        return if (lastToken == openBrack || lastToken in tokenOps || lastToken == null) {
            if (token == "-") {
                unminus
            } else {
                throw UnrecognizedTokenError(token)
            }
        } else {
            token
        }
    }

    private fun validateTokens() {
        brackStack.clear()
        for (token in tokens) {
            when (token) {
                in tokenOps -> Unit
                in brack -> {
                    brackStack.add(token)
                }

                else -> {
                    if (token.toDoubleOrNull() == null && !variableMatcher.matches(token)) {
                        throw UnrecognizedTokenError(token)
                    }
                }
            }
        }
        validateBracksAtLeastCancelEachOtherOut()
    }

    private fun validateBracksAtLeastCancelEachOtherOut() {
        while (brackStack.size > 0) {
            val element = brackStack[0]
            if (element == closeBrack) throw java.lang.IllegalStateException()
            else if (element == openBrack) {
                val nextCloseBrack = brackStack.indexOf(closeBrack)
                if (nextCloseBrack == -1) throw java.lang.IllegalStateException()
                brackStack.removeAt(nextCloseBrack)
                brackStack.removeAt(0)
            } else {
                throw UnrecognizedTokenError("brack parsing, unknown element: $element")
            }
        }
    }

    private fun replaceTokensWhereNeeded() {
        var lastToken: String? = null

        val tokenIterator = tokens.listIterator()

        while (tokenIterator.hasNext()) {
            val token = tokenIterator.next()

            when (token) {
                in tokenOps -> {
                    val resultingToken = convertTokenIfNecessary(token, lastToken)
                    tokenIterator.set(resultingToken)
                }

                in brack -> Unit
                else -> {
                    val modified = tryGetConvertedToken(token)
                    tokenIterator.set(modified)
                }
            }
            lastToken = token
        }
    }

    private fun reduceBracksAt(range: IntRange): String {
        val sublist = tokens.subList(range.first, range.last + 1)
        if (sublist.isEmpty()) throw IllegalStateException("sublist was empty for range $range")
        val opsNodes = tokenDistinguisher.createOpsNodes(sublist)
        val noBrackExprParser = NoBrackExprParser(opsNodes)
        return noBrackExprParser.fullTransformAndGetNodeId()
    }

    fun List<String>.findItem(item: String, from: Int = 0, lookForward: Boolean = true): Int {
        val range = if (lookForward) {
            from..<size
        } else {
            from.downTo(0)
        }
        for (i in range) {
            if (this[i] == item) {
                return i
            }
        }
        return -1
    }

    private fun findNextBrackRange(): IntRange? {
        val closesCloseBrack = tokens.findItem(closeBrack)
        val openBrackBefoerClosestCloseBrack = tokens.findItem(openBrack, from = closesCloseBrack, lookForward = false)
        if (closesCloseBrack == -1 || openBrackBefoerClosestCloseBrack == -1) return null
        return (openBrackBefoerClosestCloseBrack + 1)..<closesCloseBrack
    }

    private fun reduceAllBracks() {
        var insideBracksRange = findNextBrackRange()

        while (insideBracksRange != null) {
            val replacementMathNode = reduceBracksAt(insideBracksRange)
            for (i in insideBracksRange) {
                tokens.removeAt(insideBracksRange.first)
            }
            // remove the bracks
            tokens.removeAt(insideBracksRange.first - 1)
            tokens.removeAt(insideBracksRange.first - 1)
            tokens.add(insideBracksRange.first - 1, replacementMathNode)

            insideBracksRange = findNextBrackRange()
        }
    }

    private fun finalReduce(): MathNode {
        val opsNodes = tokenDistinguisher.createOpsNodes(tokens)
        val noBrackExprParser = NoBrackExprParser(opsNodes)
        return noBrackExprParser.fullTransform()
    }

    private fun tryGetConvertedToken(token: String): String {
        return if (token.toDoubleOrNull() != null) {
            token.toDoubleOrNull()!!.toString()
        } else if (variableMatcher.matches(token)) {
            "var_$token"
        } else {
            throw IllegalStateException()
        }
    }

    class OpsNodes(
        val ops: List<String>,
        val valueAndNodes: List<String>,
        val nodeSpace: NodeSpace,
    ) {
        val builtExpression
            get() = nodeSpace[valueAndNodes.first()]
    }
}