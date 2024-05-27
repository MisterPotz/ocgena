package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenArcFlowSnapshotFactory

fun List<TokenWrapper>.buildFromIndices(indices: List<Int>): List<TokenWrapper> {
    val thisList = this
    return buildList {
        for (i in indices) {
            if (i < thisList.size) {
                add(thisList[i])
            }
        }
    }
}


class OutputArcWrapper(
    val transition: TransitionWrapper,
    val toPlace: PlaceWrapper,
    private val model: ModelAccessor,
) {

    val objectType = toPlace.objectType
    val arc by lazy {
        model.ocNet.arcsRegistry.withTail(transition.id).to(toPlace.placeId)
    }

    val arcMeta: ArcMeta by lazy {
        arc.arcMeta
    }


    fun getAmountToProduce(snapshot: TokenArcFlowSnapshotFactory.Snapshot): Int {
        val arcMeta = arcMeta
        return when (arcMeta) {
            AalstVariableArcMeta -> {
                snapshot.getGroup(toPlace.objectType)?.amount ?: 0
            }

            is LomazovaVariableArcMeta -> {
                val variableName = arcMeta.variableName
                val math = arcMeta.mathNode
                val variableValue = snapshot.getVariableValue(variableName)
                math.evaluate(VariableParameterSpace(variableName to variableValue.toDouble())).toInt()
            }

            is NormalArcMeta -> {
                // must produce
                arcMeta.multiplicity
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as OutputArcWrapper

        if (transition != other.transition) return false
        if (toPlace != other.toPlace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + transition.hashCode()
        result = 31 * result + toPlace.hashCode()
        return result
    }

    override fun toString(): String {
        return "outarc(${transition.id}.${toPlace.id})"
    }
}