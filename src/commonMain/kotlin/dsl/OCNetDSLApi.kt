package dsl

import model.PlaceType

interface OCScope :
    ArcsAcceptor,
    PlaceAcceptor,
    ObjectTypeAcceptor,
    TransitionAcceptor,
    SubgraphConnector {

    fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit)

    // creates new or returns already defined
}
interface ObjectTypeAcceptor {
    fun objectType(label: String, placeNameCreator: ((placeIndexForType: Int) -> String)): ObjectTypeDSL

    fun objectType(label: String): ObjectTypeDSL
}

interface PlaceAcceptor {
    // creates new or retrieves existing
    fun place(label: String): PlaceDSL

    // creates new
    fun place(block: OCPlaceScope.() -> Unit): PlaceDSL

    // creates new or retrieves existing
    fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL

}

interface TransitionAcceptor {

    // creates new
    fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL

    // creates new or retrieves existing
    fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL

    // creates new or retrieves existing
    fun transition(label: String): TransitionDSL
}

interface SubgraphConnector {
    fun SubgraphDSL.connectTo(linkChainDSL: LinkChainDSL): HasLast
    fun HasElement.connectTo(subgraphDSL: SubgraphDSL) : SubgraphDSL

    fun subgraph(label: String? = null, block: SubgraphDSL.() -> Unit): SubgraphDSL
}

interface ArcsAcceptor {

    fun HasElement.arcTo(multiplicity: Int = 1, linkChainDSL: LinkChainDSL): HasLast
    fun HasElement.arcTo(multiplicity: Int = 1, linkChainDSL: HasElement)


    fun HasElement.variableArcTo(linkChainDSL: LinkChainDSL): HasLast

    fun HasElement.variableArcTo(hasFirst: HasElement)

}

interface ObjectTypeDSL {
    val id: Int
    val label: String
}

interface NodeDSL {
    val label: String
}

interface PlaceDSL : OCPlaceScope, LinkChainDSL, AtomDSL, NodeDSL {
    // the index of this place for its type
    var objectTypeId: Int
}

interface TransitionDSL : OCTransitionScope, LinkChainDSL, AtomDSL, NodeDSL {
    var transitionIndex: Int
}

interface AtomDSL
interface LinkChainDSL : HasLast, HasFirst {
    override val element: NodeDSL
        get() = firstElement
}

interface HasElement {
    val element : NodeDSL

    companion object {
        fun safeElement(hasElement: HasElement) : NodeDSL {
            return when(hasElement) {
                is HasFirst -> hasElement.firstElement
                is HasLast -> hasElement.lastElement
                else -> hasElement.element
            }
        }
    }
}

interface HasFirst : HasElement {
    val firstElement: NodeDSL
    override val element: NodeDSL
        get() = firstElement
}

interface HasLast : HasElement {
    val lastElement: NodeDSL
    override val element: NodeDSL
        get() = lastElement
}

interface OCTransitionScope {
    var label: String
    // delay? synchronization await time?
}

interface ArcDSL : AtomDSL {

    var tailAtom: NodeDSL
    var arrowAtom: NodeDSL

    fun isInputFor(nodeDSL: NodeDSL): Boolean
}

interface VariableArcDSL : ArcDSL
interface NormalArcDSL : ArcDSL {
    var multiplicity: Int
}

interface TypeScope : OCScope {
    val scopeType: ObjectTypeDSL
}


interface OCPlaceScope {
    var initialTokens: Int
    var objectType: ObjectTypeDSL
    val label: String
    var placeType: PlaceType
}



interface SubgraphDSL : ArcsAcceptor, TransitionAcceptor {
    val label : String
    val inNode: HasElement
    val outNode : HasElement

    fun connectOnRightTo(linkChainDSL: HasElement) : SubgraphDSL
    fun connectToLeftOf(linkChainDSL: LinkChainDSL) : HasLast
}
