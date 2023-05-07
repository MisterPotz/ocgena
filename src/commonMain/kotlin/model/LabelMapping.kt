package model

typealias Label = String
typealias NodeId = String

class LabelMapping(private val transitionsToActivity: MutableMap<NodeId, Label> = mutableMapOf()) {
    operator fun get(node: NodeId): Label {
        return transitionsToActivity[node] ?: node
    }

    operator fun get(petriNode: PetriNode) : Label {
        return transitionsToActivity[petriNode.id] ?: petriNode.id
    }
}
