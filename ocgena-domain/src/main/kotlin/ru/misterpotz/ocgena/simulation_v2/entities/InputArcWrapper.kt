package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.expression.facade.isSimpleVariable
import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.expression.paramspace.VariableParameterSpace
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.simulation_v2.entities_selection.IndependentMultiConditionGroup
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.utils.Ref
import java.util.*

class ResolvedVariablesSpace(
    private val variables: MutableMap<String, Int>
) {

    fun getVariable(variableName: String): Int {
        return variables[variableName]!!
    }

    fun setVariable(variableName: String, value: Int) {
        variables[variableName] = value
    }

    fun copyFrom(resolvedVariablesSpace: ResolvedVariablesSpace) {
        for ((variable, value) in resolvedVariablesSpace.variables) {
            this.variables[variable] = value
        }
    }

    fun copy() : ResolvedVariablesSpace {
        return ResolvedVariablesSpace(variables.toMutableMap())
    }
}

class InputArcWrapper(
    val fromPlace: PlaceWrapper,
    val transition: TransitionWrapper,
    val modelAccessor: ModelAccessor,
    val underConditions: List<MultiArcCondition>,
    val _independentGroup : Ref<IndependentMultiConditionGroup?>,
) : Comparable<InputArcWrapper> {

    override fun compareTo(other: InputArcWrapper): Int {
        return ByConditionSizeByArcSpec.compare(this, other)
    }

    fun isAalstArc() : Boolean {
        return consumptionSpec is ConsumptionSpec.AtLeastOne
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

    val independentGroup : IndependentMultiConditionGroup? by lazy {
        _independentGroup.nullable
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


    val consumptionSpec: ConsumptionSpec by lazy(LazyThreadSafetyMode.NONE) {
        val arcMeta = arcMeta
        when (arcMeta) {
            AalstVariableArcMeta -> {
                ConsumptionSpec.AtLeastOne
            }

            is LomazovaVariableArcMeta -> {
                when (arcMeta.mathNode.isSimpleVariable()) {
                    true -> ConsumptionSpec.Variable(arcMeta.variableName)
                    false -> ConsumptionSpec.DependsOnVariable(arcMeta.variableName, arcMeta.mathNode)
                }
            }

            is NormalArcMeta -> {
                ConsumptionSpec.Exact(arcMeta.multiplicity)
            }
        }
    }

    sealed interface ConsumptionSpec : Comparable<ConsumptionSpec> {

        val type: Type

        companion object {
            val comparator = compareBy<ConsumptionSpec>({
                it.type
            }, {
                when (it) {
                    // biggest required numbers come first
                    is Exact -> -it.number
                    is DependsOnVariable -> -it.initialRequirement
                    is Variable -> it.variableName
                    AtLeastOne -> 0
                }
            })
        }

        enum class Type {
            DEPENDS_ON_VAR,
            EXACT,
            VAR,
            AT_LEAST_ONE,
        }

        override fun compareTo(other: ConsumptionSpec): Int {
            return comparator.compare(this, other)
        }

        fun strongComplies(
            amount: Int,
            variablesSpace: ResolvedVariablesSpace
        ): Boolean {
            return when (this) {
                is Exact -> amount >= number
                is Variable -> amount > 0
                is DependsOnVariable -> {
                    val required = valueResolver.evaluate(
                        VariableParameterSpace(
                            variableName to variablesSpace.getVariable(variableName).toDouble()
                        )
                    )
                    amount >= required
                }

                AtLeastOne -> amount > 0
            }
        }

        fun weakComplies(
            amount: Int,
        ): Boolean {
            val requirement = tokenRequirement()
            return amount >= requirement
        }

        fun tokenRequirement(): Int {
            return when (this) {
                is Exact -> number
                is Variable -> 1
                is DependsOnVariable -> initialRequirement
                AtLeastOne -> 1
            }
        }

        fun tokensShouldTake(totalAmount: Int, variablesSpace: ResolvedVariablesSpace): Int {
            return when (this) {
                is Exact -> number
                is Variable -> totalAmount
                is DependsOnVariable -> valueResolver.evaluate(
                    VariableParameterSpace(
                        variableName to variablesSpace.getVariable(variableName).toDouble()
                    )
                ).toInt()

                AtLeastOne -> totalAmount
            }
        }

        fun isUnconstrained(): Boolean {
            return when (this) {
                is Exact -> false
                is Variable -> true
                is DependsOnVariable -> true
                AtLeastOne -> true
            }
        }

        data class Exact(val number: Int) : ConsumptionSpec {
            override val type: Type = Type.EXACT
        }

        data class Variable(
            val variableName: String
        ) : ConsumptionSpec {
            override val type: Type = Type.VAR
        }

        data class DependsOnVariable(
            val variableName: String,
            val valueResolver: MathNode
        ) : ConsumptionSpec {
            val initialRequirement = valueResolver.evaluate(VariableParameterSpace(variableName to 1.0)).toInt()
            override val type: Type = Type.DEPENDS_ON_VAR
        }

        data object AtLeastOne : ConsumptionSpec {
            override val type: Type = Type.AT_LEAST_ONE
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

    override fun toString(): String {
        return "inparc(${fromPlace.id}-${transition.id},[${underConditions.joinToString(",") { it.syncTarget.id }}])"
    }
}