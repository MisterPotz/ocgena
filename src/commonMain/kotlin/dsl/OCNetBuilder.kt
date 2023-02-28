package dsl

import model.OCNetChecker
import model.PlaceType

class OCNetBuilder {
//    fun build(block: OCScope.() -> Unit): OCNetChecker {
//        val oCScopeImpl = OCScopeImpl()
//        oCScopeImpl.block()
//
//        // TODO: convert the dsl into petri net
//
//        return OCNetChecker(places = oCScopeImpl.places)
//    }
}

interface ObjectTypeDSL {
    val label: String
}

interface NodeDSL
interface PlaceDSL : OCPlaceScope, LinkChainDSL, AtomDSL, NodeDSL {
    // the index of this place for its type
    var indexForType : Int
}

interface TransitionDSL : OCTransitionScope, LinkChainDSL, AtomDSL, NodeDSL {
    var transitionIndex : Int
}

interface AtomDSL {}
interface LinkChainDSL : HasLast, HasFirst {

//    fun selectPlace(block: (PlaceDSL).(atomIndex: Int) -> Boolean): PlaceDSL
//    fun selectTransition(block: (TransitionDSL).(atomIndex: Int) -> Boolean): TransitionDSL
}

interface HasFirst {
    val firstElement : AtomDSL
}
interface HasLast {
    val lastElement : AtomDSL
}

interface OCTransitionScope {
    var label: String
}

interface ArcDSL : AtomDSL {
    var tailAtom : AtomDSL
    var arrowAtom : AtomDSL
}

interface VariableArcDSL : ArcDSL
interface NormalArcDSL : ArcDSL {
    var multiplicity: Int
}

interface TypeScope : OCScope {
    val scopeType: ObjectTypeDSL
}

interface OCScope {
    fun selectPlace(block: (PlaceDSL).() -> Boolean): PlaceDSL
    fun selectTransition(block: (TransitionDSL).() -> Boolean): TransitionDSL

    fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit)

    // creates new or returns already defined
    fun objectType(label: String, placeNameCreator: ((placeIndexForType : Int) -> String)): ObjectTypeDSL

    fun objectType(label: String): ObjectTypeDSL

    // creates new or retrieves existing
    fun place(label: String) : PlaceDSL
    // creates new
    fun place(block: OCPlaceScope.() -> Unit): PlaceDSL
    // creates new or retrieves existing
    fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL
    // creates new
    fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL
    // creates new or retrieves existing
    fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL
    // creates new or retrieves existing
    fun transition(label: String): TransitionDSL

    infix fun LinkChainDSL.arcTo(linkChainDSL: LinkChainDSL): LinkChainDSL

    infix fun List<HasLast>.arcTo(linkChainDSL: LinkChainDSL): HasLast
    infix fun LinkChainDSL.arcTo(linkChainDSLList: List<HasFirst>): HasFirst

    fun subgraph(block : SubgraphDSL.() -> Unit) : LinkChainDSL

    fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): LinkChainDSL
    fun List<HasLast>.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): HasLast
    fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSLList: List<HasFirst>): HasFirst

    infix fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL
    infix fun List<HasLast>.variableArcTo(linkChainDSL: LinkChainDSL): HasLast
    infix fun LinkChainDSL.variableArcTo(linkChainDSLList: List<HasFirst>): HasFirst
}

interface OCPlaceScope {
    var initialTokens: Int
    var objectType: ObjectTypeDSL
    val label: String
    var placeType: PlaceType
}

interface SubgraphDSL : OCScope {
    var input : PlaceDSL
    var output: PlaceDSL
    fun setAsInputOutput(placeDSL: PlaceDSL)
}
