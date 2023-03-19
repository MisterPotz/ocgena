package dsl


class OCScopeImplCreator() {
    private val groupIdCreator: GroupsIdCreator = GroupsIdCreator().also {
        GroupIdCreatorSetupper().setupGroupIdCreator(it)
    }
    val scopeEntities = ScopeAccessibleEntities(groupsIdCreator = groupIdCreator, parentScopeEntities = null)
    val objectIdCreator: PatternIdCreator
        get() = groupIdCreator.patternIdCreatorFor("objects")

    val objectTypeCreator = ObjectTypeCreator(
        objectTypesContainer = scopeEntities,
        objectTypeIdCreator = objectIdCreator,
        groupsIdCreator = groupIdCreator
    )
    val defaultObjectType: ObjectTypeDSL =
        objectTypeCreator.createObjectType(label = "ot", placeNameCreator = { "p$it" })
    val subgraphIdCreator: PatternIdCreator
        get() = groupIdCreator.patternIdCreatorFor("subgraphs")
    val arcDelegate = ArcDelegate(arcContainer = scopeEntities)
    val placeDelegate = PlaceDelegate(
        placeCreator = PlaceCreator(
            scopeType = defaultObjectType,
            placesContainer = scopeEntities,
            groupIdCreator = groupIdCreator
        )
    )
    val transitionDelegate = TransitionDelegate(
        transitionCreator = TransitionCreator(
            transitionContainer = scopeEntities
        )
    )
}

class OCScopeImpl(
    private val rootScope: OCScopeImpl? = null,
    override val scopeType: ObjectTypeDSL,
    private val scopeEntities: ScopeAccessibleEntities,
    private val groupsIdCreator: GroupsIdCreator,
    private val placeCreator: PlaceCreator,
    private val transitionCreator: TransitionCreator,
    private val arcDelegate: ArcDelegate,
    private val placeDelegate: PlaceDelegate,
    private val transitionDelegate: TransitionDelegate,
    private val objectTypeDelegate: ObjectTypeDelegate,
    private val subgraphDelegate: SubgraphDelegate,
) : TypeScope,
    ArcsAcceptor by arcDelegate,
    PlaceAcceptor by placeDelegate,
    TransitionAcceptor by transitionDelegate,
    ObjectTypeAcceptor by objectTypeDelegate,
    SubgraphConnector by subgraphDelegate {

    override fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit) {
        val ocScopeImpl = OCScopeImpl(
            rootScope = rootScope ?: this,
            scopeType = objectTypeDSL,
            scopeEntities = ScopeAccessibleEntities(
                groupsIdCreator = groupsIdCreator,
                parentScopeEntities = ScopeAccessibleEntities(
                    groupsIdCreator = groupsIdCreator,
                    parentScopeEntities = scopeEntities
                )
            ),
            groupsIdCreator = groupsIdCreator,
            arcDelegate = arcDelegate,
            placeDelegate = placeDelegate,
            transitionDelegate = transitionDelegate,
            objectTypeDelegate = objectTypeDelegate,
            placeCreator = placeCreator,
            transitionCreator = transitionCreator,
            subgraphDelegate = subgraphDelegate
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
