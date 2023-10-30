package model.utils

import model.Arc
import model.PetriNode

class PetriNodesCopyTask(private val cachedPetriNodes: CachedPetriNodes) {
    private val createdPetriNodes: CachedPetriNodes = CachedPetriNodes()
    private fun checkOrCreatePetriNode(petriNode: PetriNode): PetriNode {
        val created = createdPetriNodes.getCachedFor(petriNode)
        if (created == null) {
            val new = petriNode.copyWithoutConnections()
            createdPetriNodes.save(new)
        }
        return createdPetriNodes.getCachedFor(petriNode)!!
    }

    private fun resolveLinksForAllNodes() {
        val keys = cachedPetriNodes.allNodeLabels()
        for (key in keys) {
            resolveLinksFor(cachedPetriNodes.getCachedFor(key)!!)
        }
    }

    private fun copyAllNodes() {
        val keys = cachedPetriNodes.allNodeLabels()
        for (key in keys) {
            checkOrCreatePetriNode(cachedPetriNodes.getCachedFor(key)!!)
        }
    }

    fun createOrFindArc(originalArc: Arc): Arc {
        val createdTailNode = createdPetriNodes.getCachedFor(originalArc.tailNode!!)!!
        val createdArrowNode = createdPetriNodes.getCachedFor(
            originalArc.arrowNode!!
        )!!
        var applicableArc: Arc? = null
        applicableArc = createdTailNode.outputArcs.find { it.arrowNode?.label == createdArrowNode.label }

        if (applicableArc != null) {
            applicableArc.arrowNode = createdArrowNode
            return applicableArc
        }
        applicableArc = createdArrowNode.inputArcs.find {
            it.tailNode?.label == createdTailNode.label }

        if (applicableArc != null) {
            applicableArc.tailNode = createdTailNode
            return applicableArc
        }

        return originalArc.copyWithTailAndArrow(createdTailNode, createdArrowNode)
    }

//    fun resolveLinksForAllNodes() {
//        val allNodes = createdPetriNodes.allNodeLabels()
//
//    }

    private fun resolveLinksFor(petriNode: PetriNode) {
        val original = cachedPetriNodes.getCachedFor(petriNode)!!
        val created = createdPetriNodes.getCachedFor(petriNode)!!

        val originalInputArcs = original.inputArcs
        val newInputArcs = originalInputArcs.map {
            createOrFindArc(it)
        }

        val originalOutputArcs = original.outputArcs
        val newOutputArcs = originalOutputArcs.map {
            createOrFindArc(it)
        }
        created.inputArcs.addAll(newInputArcs)
        created.outputArcs.addAll(newOutputArcs)
    }

    fun getCreatedNodes() : CachedPetriNodes {
        return createdPetriNodes
    }

    fun performAndGetCopiedNodes() : CachedPetriNodes {
        copyAllNodes()
        resolveLinksForAllNodes()
        return createdPetriNodes
    }
}
