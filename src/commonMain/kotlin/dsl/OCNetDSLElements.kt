package dsl

interface OCNetDSLElements : ArcContainer, PlacesContainer {
    abstract fun objectType(s: String): ObjectTypeDSL
    fun place(s: String): NodeDSL
    fun transition(s: String): NodeDSL

    override val places: MutableMap<String, PlaceDSL>

    val transitions: Map<String, TransitionDSL>

    override val arcs: MutableList<ArcDSL>

    val allPetriNodes: List<NodeDSL>

    val objectTypes : Map<String, ObjectTypeDSL>

    val defaultObjectTypeDSL: ObjectTypeDSL
}
