package model.typel

import model.Arc
import model.ArcMultiplicity

interface ExpressionArcMultiplicity : ArcMultiplicity {
    fun expressionFor(arc: Arc) : ArcExpression
}
