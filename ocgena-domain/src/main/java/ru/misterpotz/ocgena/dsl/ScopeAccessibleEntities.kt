package dsl

class ScopeAccessibleEntities(
    private val groupsIdCreator: GroupsIdCreator,
    private val parentScopeEntities: ScopeAccessibleEntities? = null,
) :
    ArcContainer,
    TransitionContainer,
    PlacesContainer,
    ObjectTypesContainer {

    override val objectTypes: MutableMap<String, ObjectTypeDSL> = parentScopeEntities?.objectTypes ?: mutableMapOf()
    override val arcs: MutableList<ArcDSL> = parentScopeEntities?.arcs ?: mutableListOf()
    override val places: MutableMap<String, PlaceDSL> = parentScopeEntities?.places ?: mutableMapOf()
    override val transitions: MutableMap<String, TransitionDSL> = parentScopeEntities?.transitions ?: mutableMapOf()
    val subgraphs : Map<String, SubgraphDSL>
        get() = _subgraphs
    private val _subgraphs : MutableMap<String, SubgraphDSL> = mutableMapOf()

    override val transitionPatternIdCreator: PatternIdCreator
        get() = groupsIdCreator.patternIdCreatorFor("t")

    val subgraphIdIssuer: PatternIdCreator
        get() = groupsIdCreator.patternIdCreatorFor("subgraph")

    fun addSubgraph(subgraphLabel : String, subgraphDSL : SubgraphDSL) {
        _subgraphs.put(subgraphLabel, subgraphDSL)
    }
}
