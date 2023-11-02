package ru.misterpotz.model.atoms

import model.ConsistencyCheckable
import model.PetriAtom
import model.PetriAtomId
import model.PetriAtomVisitorDFS
import ru.misterpotz.model.ext.arcArrowId
import ru.misterpotz.model.ext.arcTailId

abstract class Arc : ConsistencyCheckable, PetriAtom {
    val arrowNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcArrowId()
    }
    val tailNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcTailId()
    }

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

