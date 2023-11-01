package model.typel

import ru.misterpotz.model.atoms.Arc
import model.ArcMultiplicity
import model.Arcs
import ru.misterpotz.model.arcs.NormalArc
import ru.misterpotz.model.arcs.VariableArcTypeA
import ru.misterpotz.model.arcs.VariableArcTypeL

interface ExpressionArcMultiplicity : ArcMultiplicity {
    fun expressionFor(arc: Arc) : ArcExpression
}

class ArcMultiplicityTypeL(val arcs : Arcs) : ArcMultiplicity {

    fun getVariableDependency(arc: Arc) : String? {
        return when (arc) {
            is NormalArc -> null
            is VariableArcTypeA -> null
            is VariableArcTypeL -> arc.variable
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun getMultiplicity(arc: Arc, ) : Int {
        return when (arc) {
            is NormalArc -> arc.multiplicity
            is VariableArcTypeA -> 1
            is VariableArcTypeL -> TODO()
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun getAbstractTransferAmount(arc: Arc) : ArcMultiplicityTypeL.TransferAmount {
        return when(arc) {
            is NormalArc -> ArcMultiplicityTypeL.TransferAmount.Normal(arc.multiplicity)
            is VariableArcTypeA -> ArcMultiplicityTypeL.TransferAmount.SimpleVariable
            is VariableArcTypeL -> ArcMultiplicityTypeL.TransferAmount.VariableExpression(arc.expression.toString())
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun isVariable(arc: Arc) : Boolean {
        return arc !is NormalArc
    }

    sealed class TransferAmount {

        object SimpleVariable : TransferAmount() {
            override fun toString(): String {
                return "simple var"
            }
        }

        data class VariableExpression(val expression: String) : TransferAmount() {
            override fun toString(): String {
                return "var"
            }
        }
        data class Normal(val amount: Int) : TransferAmount() {
            override fun toString(): String {
                return "$amount"
            }
        }
        data class Wrong(val trace: String) : TransferAmount() {
            override fun toString(): String {
                return trace
            }
        }

        operator fun plus(transferAmount: TransferAmount) : TransferAmount {
            return when (this) {
                is Normal -> when (transferAmount) {
                    is Normal -> Normal(this.amount + transferAmount.amount)
                    else -> Wrong(toString() + "+ " + transferAmount.toString())
                }
                is VariableExpression -> Wrong(toString() + "+ " + transferAmount.toString())
                is Wrong -> Wrong(toString() + "+ " + transferAmount.toString())
                SimpleVariable -> TODO()
            }
        }
    }
}
