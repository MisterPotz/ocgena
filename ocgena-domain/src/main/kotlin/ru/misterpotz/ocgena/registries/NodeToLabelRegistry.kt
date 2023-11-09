package ru.misterpotz.ocgena.registries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.misterpotz.ocgena.ocnet.primitives.Label
import ru.misterpotz.ocgena.ocnet.primitives.NodeId
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriNode

@Serializable
data class NodeToLabelRegistry(
    @SerialName("per_transition") val transitionsToActivity: MutableMap<NodeId, Label> = mutableMapOf(),
    @SerialName("per_object_type") val objectTypeToLabel: MutableMap<ObjectTypeId, Label> = mutableMapOf()
) {
    fun getTransitionLabel(node: NodeId): Label {
        return transitionsToActivity[node] ?: node
    }

    fun getTransitionLabel(petriNode: PetriNode): Label {
        return transitionsToActivity[petriNode.id] ?: petriNode.id
    }

    fun getObjectTypeLabel(objectTypeId: ObjectTypeId): Label {
        return objectTypeToLabel[objectTypeId] ?: objectTypeId
    }

    companion object {
        fun create(block: MutableMap<NodeId, Label>.() -> Unit): NodeToLabelRegistry {
            return NodeToLabelRegistry(buildMap {
                block()
            }.toMutableMap())
        }
    }
}