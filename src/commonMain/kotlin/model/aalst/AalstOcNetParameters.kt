package model.aalst

import model.Arc
import model.ArcMultiplicity
import model.Arcs
import model.NormalArc
import model.VariableArcTypeA

class ArcMultiplicityTypeA(val arcs: Arcs) : ArcMultiplicity {
    fun getMultiplicity(arc: Arc) : Int {
        return when (arc) {
            is NormalArc -> arc.multiplicity
            is VariableArcTypeA -> 1
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun isVariable(arc: Arc) : Boolean {
        return arc is VariableArcTypeA
    }

    fun multiplicitiesEqual(arc1: Arc, arc2: Arc) : Boolean {
        val isVariable = isVariable(arc1) == isVariable(arc2)
        val multiplicities = getMultiplicity(arc1) == getMultiplicity(arc2)
        return isVariable && multiplicities
    }
}
