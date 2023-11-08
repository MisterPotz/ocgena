package ru.misterpotz.expression

import ru.misterpotz.expression.node.MathNode

class NodeSpace {
    private var nodeId = 0
    private val nodeSpace: MutableMap<String, MathNode> = mutableMapOf()

    fun addNodeAndGetId(mathNode: MathNode) : String {
        val id = "node_${(++nodeId)}"
        nodeSpace[id] = mathNode
        return id
    }

    operator fun contains(mathNodeId : String) : Boolean {
        return mathNodeId in nodeSpace.keys
    }

    operator fun get(id : String) : MathNode {
        return nodeSpace[id]!!
    }

    override fun toString(): String {
        val pairs = nodeSpace.map {
            Pair(it.key, it.value)
        }.joinToString(separator = "\n") {
            "${it.first} -> ${it.second}"
        }
        return """nodespace: 
            |${pairs.prependIndent()}
        """.trimMargin()
    }
}