@file:Suppress("UNREACHABLE_CODE")

package ru.misterpotz.ocgena.dsl

import dsl.SubgraphPlaceDelegate
import dsl.SubgraphTransitionDelegate
import dsl.TransitionCreator

class SubgraphDelegate(
    private val placeCreator: PlaceCreator,
    private val transitionCreator: TransitionCreator,
) : SubgraphConnector {
    val subgraphStruct: EntitiesCreatedInSubgraph = EntitiesCreatedInSubgraph()

    private fun recordSubgraphToThisScope(newSubgraphDSL: SubgraphDSL) {
        subgraphStruct.subgraphs[newSubgraphDSL.label] = newSubgraphDSL
    }

    private fun createSubgraph(label: String?, block: SubgraphDSL.() -> Unit) : SubgraphDSL {
        val entitiesCreatedInSubgraph = EntitiesCreatedInSubgraph()
        val subgraphConnectionResolver = SubgraphConnectionResolver()

        val newSubgraph = SubgraphImpl(
            label = throw NotImplementedError(),
            placeDelegate = SubgraphPlaceDelegate(
                entitiesCreatedInSubgraph = entitiesCreatedInSubgraph,
                placeCreator = placeCreator
            ),
            transitionDelegate = SubgraphTransitionDelegate(
                entitiesCreatedInSubgraph = entitiesCreatedInSubgraph,
                transitionCreator = transitionCreator,
            ),
            arcDelegate = SubgraphArcDelegate(
                arcContainer = throw IllegalStateException(),
                subgraphConnectionResolver = subgraphConnectionResolver,
            ),
            subgraphDelegate = SubgraphDelegate(
                placeCreator = placeCreator,
                transitionCreator = transitionCreator
            ),
            subgraphConnectionResolver = subgraphConnectionResolver
        )

        newSubgraph.block()
        return newSubgraph
    }

    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val newSubgraph = createSubgraph(label, block)
        recordSubgraphToThisScope(newSubgraph)
        return newSubgraph
    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {
        return this.connectToLeftOf(linkChainDSL)
    }

    override fun HasElement.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
        return subgraphDSL.connectOnRightTo(this)
    }
}
