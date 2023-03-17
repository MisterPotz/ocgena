package dsl

class SubgraphStruct(
    val places: MutableMap<String, PlaceDSL> = mutableMapOf(),
    val transitions: MutableMap<String, TransitionDSL> = mutableMapOf(),
    val subgraphStructs: MutableMap<String, SubgraphDSL> = mutableMapOf(),
) {

}
