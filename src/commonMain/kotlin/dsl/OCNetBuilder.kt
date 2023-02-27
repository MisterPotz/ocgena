package dsl

import model.OCNetChecker
import model.PlaceType

class OCNetBuilder {
    fun build(block: OCScope.() -> Unit) : OCNetChecker {

        return OCNetChecker()
    }

}

interface ObjectTypeDSL {
    var label: String?
}

interface PlaceDSL : OCPlaceScope, LinkChainDSL, AtomDSL {
}

interface TransitionDSL : OCTransitionScope, LinkChainDSL, AtomDSL

interface AtomDSL {}
interface LinkChainDSL {
    val orderedAtomsList: List<AtomDSL>
    fun selectPlace(block : (PlaceDSL).(atomIndex : Int) -> Boolean) : PlaceDSL
    fun selectTransition(block : (TransitionDSL).(atomIndex : Int) -> Boolean) : TransitionDSL
}

interface OCTransitionScope {
    var label: String?
}

interface VariableArcDSL : AtomDSL
interface NormalArcDSL : AtomDSL

interface OCScope {
    fun place(block: OCPlaceScope.() -> Unit): PlaceDSL
    fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL
    infix fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL
    infix fun LinkChainDSL.arcTo(linkChainDSL: LinkChainDSL): LinkChainDSL
}

interface OCPlaceScope {
    var initialTokens: Int
    var objectType: ObjectTypeDSL
    var label: String?
    var placeType: PlaceType
}

class CheckDSL {
    init {
        val ocNetBuilder = OCNetBuilder()
        ocNetBuilder.build {
            val place2 = place {

            }
            val chain = place {
                placeType = PlaceType.INPUT
            } variableArcTo transition {

            } variableArcTo place2 arcTo transition {

            }
            val firstPlace = chain.selectPlace { placeType == PlaceType.INPUT }
            firstPlace arcTo chain.selectTransition { it == 1 }
        }
    }
}
