package ru.misterpotz.ocgena.registries.typel

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import model.ArcsRegistry
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

@Suppress("UNUSED")
class ArcMultiplicityTypeL(val arcsRegistry : ArcsRegistry) : ArcMultiplicity {

    fun getVariableDependency(arc: Arc) : String? {
        return when (arc) {
            is NormalArc -> null
            is VariableArc -> null
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun getMultiplicity(arc: Arc, ) : Int {
        return when (arc) {
            is NormalArc -> arc.multiplicity
            is VariableArc -> 1
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun getAbstractTransferAmount(arc: Arc) : TransferAmount {
        return when(arc) {
            is NormalArc -> TransferAmount.Normal(arc.multiplicity)
            is VariableArc -> TransferAmount.SimpleVariable
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
