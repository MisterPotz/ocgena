package dsl

import model.PlaceType

class LinkChainDSLImpl(
    override val firstElement: AtomDSL,
    override val lastElement: AtomDSL) : LinkChainDSL {
}

class VariableArcDSLImpl(
    override var tailAtom: AtomDSL,
    override var arrowAtom: AtomDSL,
) : VariableArcDSL {
    override fun toString(): String {
        return "var arc [ $tailAtom => $arrowAtom ]"
    }
}

class NormalArcDSLImpl(
    override var multiplicity: Int,
    override var tailAtom: AtomDSL,
    override var arrowAtom: AtomDSL,
) : NormalArcDSL {
    override fun toString(): String {
        return "norm arc [ $tailAtom -> $arrowAtom ]"
    }
}

class HasLastImpl(override val lastElement: AtomDSL) : HasLast
class HasFirstImpl(override val firstElement: AtomDSL) : HasFirst

class ObjectTypeImpl(
    val id: Int,
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
    override val firstElement: AtomDSL
        get() = this
    override val lastElement: AtomDSL
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

    override val firstElement: AtomDSL
        get() = this
    override val lastElement: AtomDSL
        get() = this
    override var label: String = defaultLabel

    override fun toString(): String {
        return "transition [ $label ]"
    }
}

