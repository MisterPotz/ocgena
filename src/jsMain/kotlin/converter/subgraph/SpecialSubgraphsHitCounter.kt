package converter.subgraph

import ast.SubgraphSpecialTypes

class SpecialSubgraphsHitCounter {
    val specialSubgraphTypeToHits = mutableMapOf<String, Int>()


    fun hit(specialSubgraphType: String) {
        specialSubgraphTypeToHits[specialSubgraphType] = (specialSubgraphTypeToHits[specialSubgraphType] ?: 0) + 1
    }

    fun hitsFor(specialSubgraphType: String) : Int {
        return specialSubgraphTypeToHits[specialSubgraphType] ?: 0
    }

    fun canHit(specialSubgraphType: String): Boolean {
        val current = hitsFor(specialSubgraphType)
        return when (specialSubgraphType) {
            SubgraphSpecialTypes.ObjectTypes,
            SubgraphSpecialTypes.Places,
            SubgraphSpecialTypes.Transitions,
            SubgraphSpecialTypes.InitialMarking,
            SubgraphSpecialTypes.Inputs,
            SubgraphSpecialTypes.Outputs -> current == 0
            else -> {
                if (specialSubgraphType.startsWith(SubgraphSpecialTypes.PlacesForType)) {
                    current == 0
                } else {
                    false
                }
            }
        }
    }

    companion object {
        fun getFullNameFor(specialSubgraphType: String, subgraphId : String?) : String {
            return specialSubgraphType+(subgraphId ?: "")
        }
    }
}
