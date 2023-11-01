package ru.misterpotz.model.atoms

import model.ConsistencyCheckable
import model.PetriAtom
import model.PetriAtomId
import model.PetriAtomVisitorDFS
import ru.misterpotz.input.converter.ext.arcArrowId
import ru.misterpotz.input.converter.ext.arcTailId


abstract class Arc : ConsistencyCheckable, PetriAtom {
    var arrowNodeId: PetriAtomId = id.arcArrowId()
    var tailNodeId: PetriAtomId = id.arcTailId()

    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitArc(this)
    }

    @Deprecated("incorrect")
    fun requireTailPlace(): Place {
        return checkNotNull(tailNodeId as? Place) {
            "tail place was required not null"
        }
    }

    @Deprecated("incorrect")
    fun requireTailTransition(): Transition {
        return checkNotNull(tailNodeId as? Transition) {
            "tail transition was required to be not null"
        }
    }

    @Deprecated("incorrect")
    fun requireArrowTransition(): Transition {
        return checkNotNull(arrowNodeId as? Transition) {
            "tail transition was required to be not null"
        }
    }

    @Deprecated("incorrect")
    fun requireArrowPlace(): Place {
        return checkNotNull(arrowNodeId as? Place) {
            "arrow place was required not null"
        }
    }

    abstract fun isSameArcType(other: Arc): Boolean
}

