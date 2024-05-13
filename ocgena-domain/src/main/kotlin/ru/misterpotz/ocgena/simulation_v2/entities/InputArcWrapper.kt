package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SortedTokens
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import java.util.*

class InputArcWrapper(
    val fromPlace: PlaceWrapper,
    val transition: TransitionWrapper,
    val modelAccessor: ModelAccessor,
    val underConditions: List<MultiArcCondition>
) : Comparable<InputArcWrapper> {

    override fun compareTo(other: InputArcWrapper): Int {
        return ByConditionSizeByArcSpec.compare(this, other)
    }

    companion object {
        val ByConditionSizeByArcSpecByTransitionEntries = compareBy<InputArcWrapper>({
            -it.underConditions.size
        }, {
            it.consumptionSpec
        }, {
            -it.transitionHistorySize
        })

        val ByConditionSizeByArcSpec = compareBy<InputArcWrapper>({
            -it.underConditions.size
        }, {
            it.consumptionSpec
        })
    }

    val syncTransitions by lazy {
        buildSet {
            underConditions.forEach {
                add(it.syncTarget)
            }
        }
    }

    val arc by lazy {
        modelAccessor.ocNet.arcsRegistry.withArrow(transition.id).from(fromPlace.placeId)
    }

    val arcMeta: ArcMeta by lazy {
        arc.arcMeta
    }

    val transitionHistorySize
        get() = transition.transitionHistory.size()

    val allAssociatedConditions: SortedSet<MultiArcCondition> by lazy(LazyThreadSafetyMode.NONE) {
        val allAssociatedArcs = underConditions.flatMap { it.arcs.ref }.toSet()
        allAssociatedArcs.flatMap {
            it.underConditions
        }.toSortedSet()
    }

    val allAssociatedArcs: Set<InputArcWrapper> by lazy(LazyThreadSafetyMode.NONE) {
        underConditions.flatMap { it.arcs.ref }.toMutableSet().apply {
            remove(this@InputArcWrapper)
        }
    }

    var currentSolutionSeachFilteredTokens: MutableSet<TokenWrapper>? = null

    fun selectApplicableTokens(tokenSlice: TokenSlice) {
        currentSolutionSeachFilteredTokens?.clear()
        currentSolutionSeachFilteredTokens = mutableSetOf()
        val applicable = tokenSlice.tokensAt(fromPlace).filter { token ->
            // найти только те токены, что поучаствовали во всех транзишенах строгого условия
            syncTransitions.all { it in token.visitedTransitions }
        }
        currentSolutionSeachFilteredTokens!!.addAll(applicable)
    }


    fun checkAmount(amount: Int): Boolean {
        return consumptionSpec.complies(amount)
    }

    fun getTokens(tokenSlice: TokenSlice): SortedTokens {
        return tokenSlice.tokensAt(fromPlace)
    }

    val consumptionSpec: ConsumptionSpec by lazy(LazyThreadSafetyMode.NONE) {
        when (arcMeta) {
            AalstVariableArcMeta -> {
                ConsumptionSpec.AtLeastOne
            }

            is LomazovaVariableArcMeta -> {
                ConsumptionSpec.AtLeastOne
            }

            is NormalArcMeta -> {
                ConsumptionSpec.Exact((arcMeta as NormalArcMeta).multiplicity)
            }
        }
    }

    sealed interface ConsumptionSpec : Comparable<ConsumptionSpec> {

        fun complies(amount: Int): Boolean {
            return when (this) {
                AtLeastOne -> amount > 0
                is Exact -> amount >= number
            }
        }

        fun tokenRequirement(): Int {
            return when (this) {
                AtLeastOne -> 1
                is Exact -> number
            }
        }

        data class Exact(val number: Int) : ConsumptionSpec {
            override fun compareTo(other: ConsumptionSpec): Int {
                return when (other) {
                    AtLeastOne -> number.compareTo(1)
                    is Exact -> number.compareTo(other.number)
                }
            }
        }

        data object AtLeastOne : ConsumptionSpec {
            override fun compareTo(other: ConsumptionSpec): Int {
                return when (other) {
                    AtLeastOne -> 0
                    is Exact -> -1
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InputArcWrapper

        if (fromPlace != other.fromPlace) return false
        if (transition != other.transition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromPlace.hashCode()
        result = 31 * result + transition.hashCode()
        return result
    }
}