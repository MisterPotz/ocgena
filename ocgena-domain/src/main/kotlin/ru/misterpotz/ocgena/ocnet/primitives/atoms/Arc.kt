package ru.misterpotz.ocgena.ocnet.primitives.atoms

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcArrowId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcTailId
import ru.misterpotz.ocgena.validation.PetriAtomVisitorDFS

enum class ArcType {
    NORMAL,
    VARIABLE,
}


abstract class Arc : PetriAtom {
    val arrowNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcArrowId()
    }
    val tailNodeId: PetriAtomId? by lazy(LazyThreadSafetyMode.NONE) {
        id.arcTailId()
    }
    abstract val arcMeta : ArcMeta
    abstract val arcType : ArcType
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitArc(this)
    }

    abstract fun isSameArcType(other: Arc): Boolean
}

