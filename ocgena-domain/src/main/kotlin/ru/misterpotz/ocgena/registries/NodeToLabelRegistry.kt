package ru.misterpotz.ocgena.registries

import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.Label
import ru.misterpotz.ocgena.ocnet.primitives.NodeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriNode

@Serializable
data class NodeToLabelRegistry(private val transitionsToActivity: MutableMap<NodeId, Label> = mutableMapOf()) {
    operator fun get(node: NodeId): Label {
        return transitionsToActivity[node] ?: node
    }

    operator fun get(petriNode: PetriNode): Label {
        return transitionsToActivity[petriNode.id] ?: petriNode.id
    }

    companion object {
        fun create(block: MutableMap<NodeId, Label>.() -> Unit): NodeToLabelRegistry {
            return NodeToLabelRegistry(buildMap {
                block()
            }.toMutableMap())
        }
    }
}