package dsl

import ru.misterpotz.ocgena.dsl.ArcDelegate
import ru.misterpotz.ocgena.dsl.GroupsIdCreator
import ru.misterpotz.ocgena.dsl.ObjectTypeDelegate


class OCScopeImpl(
    private val defaultObjectType : ObjectTypeDSL,
    override val scopeType: ObjectTypeDSL,
    private val scopeAccessibleEntities: ScopeAccessibleEntities,
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

    fun ocNetElements(): OCNetDSLElements {
        return OCNetDSLElementsImpl(
            places = scopeAccessibleEntities.places,
            transitions = scopeAccessibleEntities.transitions,
            arcs = scopeAccessibleEntities.arcs,
            objectTypes = scopeAccessibleEntities.objectTypes,
            defaultObjectTypeDSL = defaultObjectType
        )
    }

    override fun elementByLabel(label: String): LinkChainDSL? {
        val place = scopeAccessibleEntities.places[label]
        if (place != null) {
            return place
        }
        return scopeAccessibleEntities.transitions[label]
    }

    override fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit) {
        val ocScopeImpl = createChildOCSCope(objectTypeDSL)
        ocScopeImpl.block()
    }

    private fun createChildOCSCope(objectTypeDSL: ObjectTypeDSL): OCScopeImpl {
        return OCScopeImpl(
            defaultObjectType = defaultObjectType,
            scopeType = objectTypeDSL,
            scopeAccessibleEntities = ScopeAccessibleEntities(
                groupsIdCreator = groupsIdCreator,
                parentScopeEntities = ScopeAccessibleEntities(
                    groupsIdCreator = groupsIdCreator,
                    parentScopeEntities = scopeAccessibleEntities
                )
            ),
            groupsIdCreator = groupsIdCreator,
            arcDelegate = arcDelegate,
            placeDelegate = PlaceDelegate(
                placeCreator = PlaceCreator(
                    objectTypeDSL,
                    placesContainer = scopeAccessibleEntities,
                    groupIdCreator = groupsIdCreator
                )
            ),
            transitionDelegate = transitionDelegate,
            objectTypeDelegate = objectTypeDelegate,
            placeCreator = placeCreator,
            transitionCreator = transitionCreator,
            subgraphDelegate = subgraphDelegate,
        )
    }

    companion object {
        const val DEFAULT_PLACE_PREFIX = "p"
        const val DEFAULT_TRANSITION_PREFIX = "t"
        const val DEFAULT_OBJECT_TYPE = "ot"
        const val USER_SAFE_PREFIX = "u"
    }
}
