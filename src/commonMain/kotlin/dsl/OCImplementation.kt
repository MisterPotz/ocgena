package dsl

import model.PlaceType

class LinkChainDSLImpl : LinkChainDSL {
    override val orderedAtomsList: MutableList<AtomDSL> = mutableListOf()

    override val firstElement: AtomDSL
        get() = orderedAtomsList.first()
    override val lastElement: AtomDSL
        get() = orderedAtomsList.last()

    override fun selectPlace(block: PlaceDSL.(atomIndex: Int) -> Boolean): PlaceDSL {
        return Utils.selectPlace(orderedAtomsList, block)
    }

    override fun selectTransition(block: TransitionDSL.(atomIndex: Int) -> Boolean): TransitionDSL {
        return Utils.selectTransition(orderedAtomsList, block)
    }
}

class VariableArcDSLImpl(
    override var tailAtom: AtomDSL,
    override var arrowAtom: AtomDSL,
) : VariableArcDSL {
}

class NormalArcDSLImpl(
    override var multiplicity: Int,
    override var tailAtom: AtomDSL,
    override var arrowAtom: AtomDSL,
) : NormalArcDSL {

}

class HasLastImpl(override val lastElement: AtomDSL) : HasLast
class HasFirstImpl(override val firstElement: AtomDSL) : HasFirst

class ObjectTypeImpl(val id: String, override val label: String) : ObjectTypeDSL {
}

class PlaceDSLImpl(
//    private val defaultLabelFactory: () -> String,
    private val onAssignNewObjectType: (ObjectTypeDSL) -> Unit,
    private val onAssignNewLabel: (String) -> Unit,
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

    override var label: String = ""
        get() {
            return labelFactory()
        }
        set(value) {
            onAssignNewLabel(value)
            field = value
        }

    override var initialTokens: Int = 0
    override var placeType: PlaceType = PlaceType.NORMAL

    override val orderedAtomsList: List<AtomDSL> = listOf(this)

    override fun selectPlace(block: PlaceDSL.(atomIndex: Int) -> Boolean): PlaceDSL {
        return Utils.selectPlace(orderedAtomsList, block)
    }

    override fun selectTransition(block: TransitionDSL.(atomIndex: Int) -> Boolean): TransitionDSL {
        return Utils.selectTransition(orderedAtomsList, block)
    }
}

class TransitionDSLImpl(
    defaultLabel: String,
) : TransitionDSL {
    override val orderedAtomsList: List<AtomDSL> = listOf(this)
    var userAssignedLabel: Boolean = false

    override val firstElement: AtomDSL
        get() = this
    override val lastElement: AtomDSL
        get() = this
    override var label: String = defaultLabel
        set(value) {
            userAssignedLabel = true
            field = value
        }

    override fun selectPlace(block: PlaceDSL.(atomIndex: Int) -> Boolean): PlaceDSL {
        return Utils.selectPlace(orderedAtomsList, block)
    }

    override fun selectTransition(block: TransitionDSL.(atomIndex: Int) -> Boolean): TransitionDSL {
        return Utils.selectTransition(orderedAtomsList, block)
    }
}

