package ru.misterpotz.ocgena.registries.typea

import ru.misterpotz.ocgena.ocnet.primitives.ArcMultiplicity
import model.ArcsRegistry
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcTypeA
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc

class ArcToMultiplicityTypeARegistry(val arcsRegistry: ArcsRegistry) : ArcMultiplicity {
    fun getMultiplicity(arc: Arc) : Int {
        return when (arc) {
            is NormalArc -> arc.multiplicity
            is VariableArcTypeA -> 1
            else -> throw IllegalStateException("can't calculate arc multiplicity for unsupported type")
        }
    }

    fun getAbstractTransferAmount(arc: Arc) : TransferAmount {
        return when(arc) {
            is NormalArc -> TransferAmount.Normal(arc.multiplicity)
            is VariableArcTypeA -> TransferAmount.Variable
            else -> TransferAmount.Wrong(trace = "[unknown arc ${arc::class.simpleName}]")
        }
    }

    fun isVariable(arc: Arc) : Boolean {
        return arc !is NormalArc
    }


    fun multiplicitiesEqual(arc1: Arc, arc2: Arc) : Boolean {
        val isVariable = isVariable(arc1) == isVariable(arc2)
        val multiplicities = getMultiplicity(arc1) == getMultiplicity(arc2)
        return isVariable && multiplicities
    }

    sealed class TransferAmount {
        object Variable : TransferAmount() {
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
                Variable -> Wrong(toString() + "+ " + transferAmount.toString())
                is Wrong -> Wrong(toString() + "+ " + transferAmount.toString())
            }
        }
    }
}
