package dsl

class OCScopeImplCreator() {
    private val groupIdCreator: GroupsIdCreator = GroupsIdCreator().also {
        GroupIdCreatorSetupper().setupGroupIdCreator(it)
    }
    private val scopeAccessibleEntities =
        ScopeAccessibleEntities(
            groupsIdCreator = groupIdCreator,
            parentScopeEntities = null)
    private val objectIdCreator: PatternIdCreator
        get() = groupIdCreator.patternIdCreatorFor("objects")

    private val objectTypeCreator = ObjectTypeCreator(
        objectTypesContainer = scopeAccessibleEntities,
        objectTypeIdCreator = objectIdCreator,
        groupsIdCreator = groupIdCreator
    )
    private val defaultObjectType: ObjectTypeDSL =
        objectTypeCreator.createObjectType(label = "ot", placeNameCreator = { "p$it" })
    private val arcDelegate = ArcDelegate(arcContainer = scopeAccessibleEntities)
    private val placeCreator = PlaceCreator(
        scopeType = defaultObjectType,
        placesContainer = scopeAccessibleEntities,
        groupIdCreator = groupIdCreator
    )
    private val placeDelegate = PlaceDelegate(
        placeCreator = placeCreator
    )
    private val transitionCreator = TransitionCreator(
        transitionContainer = scopeAccessibleEntities
    )
    private val transitionDelegate = TransitionDelegate(
        transitionCreator = transitionCreator
    )
    private val objectTypeDelegate = ObjectTypeDelegate(
        objectTypeCreator = objectTypeCreator
    )

    private val subgraphDelegate = SubgraphDelegate(
        scopeAccessibleEntities = scopeAccessibleEntities,
        placeCreator = placeCreator,
        transitionCreator = transitionCreator
    )

    fun createRootOCScope(): OCScopeImpl {

        return OCScopeImpl(
            defaultObjectType = defaultObjectType,
            scopeType = defaultObjectType,
            scopeAccessibleEntities = scopeAccessibleEntities,
            groupsIdCreator = groupIdCreator,
            placeCreator = placeCreator,
            transitionCreator = transitionCreator,
            arcDelegate = arcDelegate,
            placeDelegate = placeDelegate,
            transitionDelegate = transitionDelegate,
            objectTypeDelegate = objectTypeDelegate,
            subgraphDelegate = subgraphDelegate,

        )
    }
}
