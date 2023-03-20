package model

import dsl.ArcDSL
import dsl.NodeDSL
import dsl.PlaceDSL
import dsl.TransitionDSL

class ElementsIdCreator() {
    fun createPlaceId(placeDSL: PlaceDSL) : String {
        return placeDSL.label
    }

    fun createTransitionId(transitionDSL: TransitionDSL) : String {
        return transitionDSL.label
    }

    fun createNodeId(nodeDSL: NodeDSL) : String {
        return when (nodeDSL) {
            is PlaceDSL -> createPlaceId(nodeDSL)
            is TransitionDSL -> createTransitionId(nodeDSL)
            else -> throw IllegalArgumentException("nodeDSL type $nodeDSL is not supported ${nodeDSL::class}")
        }
    }

    fun createArcId(arcDSL: ArcDSL) : String {
        val fromId = createNodeId(nodeDSL = arcDSL.tailAtom)
        val toId = createNodeId(nodeDSL = arcDSL.arrowAtom)
        return "${fromId}_${toId}_${arcDSL.arcIndexForTailAtom}"
    }
}
