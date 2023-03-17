package dsl

import model.PlaceType

interface OCScope :
    ArcsAcceptor,
    PlaceAcceptor,
    TransitionAcceptor,
    SubgraphConnector {
    fun selectPlace(block: (PlaceDSL).() -> Boolean): PlaceDSL
    fun selectTransition(block: (TransitionDSL).() -> Boolean): TransitionDSL

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
    fun LinkChainDSL.connectTo(subgraphDSL: SubgraphDSL) : SubgraphDSL

    fun subgraph(label: String? = null, block: SubgraphDSL.() -> Unit): SubgraphDSL
}

interface ArcsAcceptor {

    fun HasLast.arcTo(multiplicity: Int = 1, linkChainDSL: LinkChainDSL): HasLast
    fun HasLast.arcTo(multiplicity: Int = 1, linkChainDSL: HasFirst)

    fun LinkChainDSL.arcTo(multiplicity: Int = 1, linkChainDSL: LinkChainDSL): LinkChainDSL


    fun HasLast.variableArcTo(linkChainDSL: LinkChainDSL): HasLast

    fun HasLast.variableArcTo(hasFirst: HasFirst)

    fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL
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
interface LinkChainDSL : HasLast, HasFirst

interface HasFirst {
    val firstElement: NodeDSL
}

interface HasLast {
    val lastElement: NodeDSL
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

    val inArcsWithConnectedArrows : List<ArcDSL>
    val outArcsWithConnectedTails : List<ArcDSL>
    val inNode: HasLast
    val outNode : HasFirst
}
