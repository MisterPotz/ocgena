package model.aalst

import model.Arc
import model.ArcMultiplicity

interface StaticArcMultiplicity : ArcMultiplicity {
    fun getMultiplicity(arc: Arc) : Int

    fun isVariable(arc: Arc) : Boolean

    fun multiplicitiesEqual(arc1: Arc, arc2: Arc) : Boolean
}
