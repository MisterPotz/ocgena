package dsl

import model.PlaceType

class LinkChainDSLImpl(
    override val firstElement: NodeDSL,
    override val lastElement: NodeDSL) : LinkChainDSL {
}

class VariableArcDSLImpl(
    override var tailAtom: NodeDSL,
    override var arrowAtom: NodeDSL,
) : VariableArcDSL {
    override fun isInputFor(nodeDSL: NodeDSL): Boolean {
        return arrowAtom == nodeDSL
    }
    override fun toString(): String {
        return "var arc [ $tailAtom => $arrowAtom ]"
    }
}

class NormalArcDSLImpl(
    override var multiplicity: Int,
    override var tailAtom: NodeDSL,
    override var arrowAtom: NodeDSL,
) : NormalArcDSL {
    override fun isInputFor(nodeDSL: NodeDSL): Boolean {
        return arrowAtom == nodeDSL
    }

    override fun toString(): String {
        return "norm arc [ $tailAtom -> $arrowAtom ]"
    }
}

class HasLastImpl(override val lastElement: NodeDSL) : HasLast
class HasFirstImpl(override val firstElement: NodeDSL) : HasFirst

class ObjectTypeImpl(
    override val id: Int,
    override val label: String,
) : ObjectTypeDSL {
    override fun toString(): String {
        return label
    }
}

class PlaceDSLImpl(
    override var indexForType: Int,
//    private val defaultLabelFactory: () -> String,
    private val onAssignNewObjectType: (ObjectTypeDSL) -> Unit,
    private val labelFactory: () -> String,
    objectType: ObjectTypeDSL,
) : PlaceDSL {
    override val firstElement: NodeDSL
        get() = this
    override val lastElement: NodeDSL
        get() = this
    override var objectType: ObjectTypeDSL = objectType
        set(value) {
            onAssignNewObjectType(value)
            field = value
        }

    var finalValue : String? = null
    override val label: String
        get() = finalValue ?: labelFactory()

    override var initialTokens: Int = 0
    override var placeType: PlaceType = PlaceType.NORMAL

    override fun toString(): String {
        return "place [$label [ $objectType ]]"
    }
}

class TransitionDSLImpl(
    override var transitionIndex: Int,
    defaultLabel: String,
) : TransitionDSL {

    override val firstElement: NodeDSL
        get() = this
    override val lastElement: NodeDSL
        get() = this
    override var label: String = defaultLabel

    override fun toString(): String {
        return "transition [ $label ]"
    }
}

