package ru.misterpotz.ocgena.ocnet.primitives.atoms

import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
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
    abstract val arcMeta: ArcMeta
    abstract val arcType: ArcType
    override fun acceptVisitor(visitor: PetriAtomVisitorDFS) {
        visitor.visitArc(this)
    }

    val variableName: String? by lazy(LazyThreadSafetyMode.NONE) {
        val arcmeta = arcMeta
        when (arcmeta) {
            AalstVariableArcMeta -> null
            is LomazovaVariableArcMeta -> arcmeta.variableName
            is NormalArcMeta -> null
        }
    }

    val mathNode: MathNode? by lazy(LazyThreadSafetyMode.NONE) {
        val arcmeta = arcMeta
        when (arcmeta) {
            AalstVariableArcMeta -> null
            is LomazovaVariableArcMeta -> arcmeta.mathNode
            is NormalArcMeta -> null
        }
    }

    abstract fun isSameArcType(other: Arc): Boolean
}

