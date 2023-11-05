package ru.misterpotz.ocgena.ocnet.primitives.atoms

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcArrowId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcTailId
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS

enum class ArcType {
    NORMAL,
    VARIABLE
}

abstract class Arc : PetriAtom {
    val arrowNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcArrowId()
    }
    val tailNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcTailId()
    }
    abstract val arcType : ArcType

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

