package dsl

class OCScopeImpl(
    private val rootScope: OCScopeImpl? = null,
    private val defaultScopeType: ObjectTypeDSL? = null,
    private val arcDelegate: ArcDelegate = ArcDelegate(
        arcs = rootScope?.arcDelegate?.arcs ?: mutableListOf()
    ),
    private val placeDelegate: PlaceDelegate = PlaceDelegate(
        placeCreator = null
    ),
    private val transitionDelegate: TransitionDelegate = TransitionDelegate(),
    private val objectTypeDelegate: ObjectTypeDelegate = ObjectTypeDelegate()
) : TypeScope,
    ArcsAcceptor by arcDelegate,
    ArcContainer,
    PlacesContainer,
    PlaceAcceptor by placeDelegate,
    TransitionContainer,
    TransitionAcceptor by transitionDelegate,
    ObjectTypesContainer,
    ObjectTypeAcceptor by objectTypeDelegate {

    override val objectTypes: MutableMap<String, ObjectTypeDSL> = rootScope?.objectTypes ?: mutableMapOf()
    private val groupIdIssuer: GroupsIdCreator =
        rootScope?.groupIdIssuer ?: GroupsIdCreator()
    private val objectIdIssuer: PatternIdCreator
        get() = groupIdIssuer.patternIdCreatorFor("objects")

    init {
        objectTypeDelegate.lateinitObjectTypeCreator = ObjectTypeCreator(
            objectTypesContainer = this,
            objectTypeIdCreator = objectIdIssuer,
            groupsIdCreator = groupIdIssuer
        )
    }
    override val defaultObjectType: ObjectTypeDSL = rootScope?.defaultObjectType
            ?: objectType(label = "ot", placeNameCreator = {
                "p$it"
            })

    private val assignedScopeTypeDSL: ObjectTypeDSL = defaultScopeType ?: defaultObjectType

    init {
        placeDelegate.placeCreator = PlaceCreator(
            scopeType = assignedScopeTypeDSL,
            placesContainer = this,
            groupIdIssuer = groupIdIssuer
        )
        transitionDelegate.lateAssignTransitionCreator = TransitionCreator(
            transitionContainer = this
        )
    }


    override val places: MutableMap<String, PlaceDSL>
        get() = rootScope?.places ?: mutableMapOf()
    override val transitions: MutableMap<String, TransitionDSL>
        get() = rootScope?.transitions ?: mutableMapOf()

    private val subgraphStruct = SubgraphStruct(
        places = mutableMapOf(),
        transitions = mutableMapOf(),
        subgraphStructs = mutableMapOf()
    )

    override val arcs: List<ArcDSL>
        get() = arcDelegate.arcs


    init {
        if (rootScope == null) {
            groupIdIssuer.addPatternIdCreatorFor("t", startIndex = 1) {
                "t$it"
            }
            groupIdIssuer.addPatternIdCreatorFor("subgraph", startIndex = 1) {
                "subgraph$it"
            }
        }
    }

    override val transitionPatternIdCreator: PatternIdCreator
        get() = groupIdIssuer.patternIdCreatorFor("t")


    private val subgraphIdIssuer: PatternIdCreator
        get() = groupIdIssuer.patternIdCreatorFor("subgraph")


    private val defaultPlaceIdIssuer: PatternIdCreator
        get() = groupIdIssuer.patternIdCreatorFor("p")

    override val scopeType: ObjectTypeDSL
        get() = checkNotNull(defaultScopeType)

    override fun selectPlace(block: PlaceDSL.() -> Boolean): PlaceDSL {
        return places.values.first { atomDSL ->
            atomDSL.block()
        }
    }
    override fun subgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        val newSubgraph = internalCreateSubgraph(label, block)
        recordSubgraphToThisScope(newSubgraph)
        return newSubgraph
    }


    private fun recordSubgraphToThisScope(newSubgraphDSL: SubgraphDSL) {
        subgraphStruct.subgraphStructs[newSubgraphDSL.label] = newSubgraphDSL
    }

    internal fun internalCreateSubgraph(label: String?, block: SubgraphDSL.() -> Unit): SubgraphDSL {
        subgraphIdIssuer.newIntId()

        val newSubgraph = SubgraphImpl(
            label = label ?: subgraphIdIssuer.lastLabelId,
            rootScope = rootScope ?: this
        )
        newSubgraph.block()
        // don't add this subgraph to local subgraphs
        return newSubgraph
    }

    override fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL): SubgraphDSL {
        val tailNode = this.lastElement
        subgraphDSL.stru
    }

    override fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast {

    }

    override fun selectTransition(block: TransitionDSL.() -> Boolean): TransitionDSL {
        return transitions.values.first { transitionDSL ->
            transitionDSL.block()
        }
    }

    override fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit) {
        val ocScopeImpl = OCScopeImpl(
            rootScope = rootScope ?: this,
            defaultScopeType = objectTypeDSL
        )
        ocScopeImpl.block()
    }

    companion object {
        const val DEFAULT_PLACE_PREFIX = "p"
        const val DEFAULT_TRANSITION_PREFIX = "t"
        const val DEFAULT_OBJECT_TYPE = "ot"
        const val USER_SAFE_PREFIX = "u"
    }
}
