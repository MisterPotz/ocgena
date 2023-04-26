package dsl

import model.PlaceType

class LinkChainDSLImpl(
    override val firstElement: NodeDSL,
    override val lastElement: NodeDSL) : LinkChainDSL {
}

class VariableArcDSLImpl(
    override var tailAtom: NodeDSL,
    override var arcIndexForTailAtom: Int,
    override var arrowAtom: NodeDSL,
) : VariableArcDSL {
    override fun isInputFor(nodeDSL: NodeDSL): Boolean {
        return arrowAtom == nodeDSL
    }

    override fun isOutputFor(nodeDSL: NodeDSL): Boolean {
        return tailAtom == nodeDSL
    }

    override fun toString(): String {
        return "var arc [ $tailAtom => $arrowAtom ]"
    }
}

class NormalArcDSLImpl(
    override var multiplicity: Int,
    override var tailAtom: NodeDSL,
    override var arcIndexForTailAtom: Int,
    override var arrowAtom: NodeDSL,
) : NormalArcDSL {

    override fun isInputFor(nodeDSL: NodeDSL): Boolean {
        return arrowAtom == nodeDSL
    }

    override fun isOutputFor(nodeDSL: NodeDSL): Boolean {
        return tailAtom == nodeDSL
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ObjectTypeImpl

        if (id != other.id) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + label.hashCode()
        return result
    }
}

class PlaceDSLImpl(
    override var objectTypeId: Int,
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

    var finalLabel : String? = null
    override val label: String
        get() = finalLabel ?: labelFactory()

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

