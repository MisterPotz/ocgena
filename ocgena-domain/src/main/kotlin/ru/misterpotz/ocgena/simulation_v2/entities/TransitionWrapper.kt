package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.*
import ru.misterpotz.ocgena.simulation_v2.entities_storage.*
import ru.misterpotz.ocgena.simulation_v2.utils.Identifiable
import ru.misterpotz.ocgena.simulation_v2.utils.Ref
import ru.misterpotz.ocgena.simulation_v2.utils.selectIn
import ru.misterpotz.ocgena.utils.TimePNRef
import ru.misterpotz.ocgena.utils.cast

class SolutionCache {
    var solution: FullSolution? = null

    val needCheckCache: Boolean
        get() = solution == null || solution is FullSolution.DoesNotExistSynchronized

    fun undoSolution(tokenStore: TokenStore) {
        val solution = solution
        when (solution) {
            is FullSolution.Amounts, null -> Unit
            is FullSolution.Tokens -> tokenStore.removeTokens(solution.generatedTokens)
            is FullSolution.DoesNotExistSynchronized -> Unit
        }
        this.solution = null
    }
}

fun InputArcWrapper.ConsumptionSpec.castVar(): InputArcWrapper.ConsumptionSpec.Variable {
    return this as InputArcWrapper.ConsumptionSpec.Variable
}

fun InputArcWrapper.ConsumptionSpec.castDependent(): InputArcWrapper.ConsumptionSpec.DependsOnVariable? {
    return this as? InputArcWrapper.ConsumptionSpec.DependsOnVariable?
}

var EXPERIMENT_V2_SOLVER = true

class TransitionWrapper(
    val transitionId: PetriAtomId,
    val model: ModelAccessor,
) : Identifiable, Comparable<TransitionWrapper> {
    val solutionCache = SolutionCache()

    val timer: TransitionTimer by lazy {
        val eftLft =
            model.simulationInput.transitions[transitionId]?.eftLft?.values
                ?: model.simulationInput.defaultEftLft?.values ?: 0..0

        TransitionTimer(0, eft = eftLft.first.toLong(), lft = eftLft.last.toLong())
    }

    fun cacheSolution(solution: FullSolution) {
        println("$this has solution ${solution.nice()}")
        solutionCache.solution = solution
    }

    fun FullSolution.nice(): String {
        return if (this is FullSolution.DoesNotExistSynchronized) "NONE" else ""
    }

    fun needCheckCache(): Boolean {
        return solutionCache.needCheckCache
    }

    companion object {
        val comparator = compareBy<TransitionWrapper> {
            it.transitionId
        }
    }

    fun findInputArcByPlace(placeWrapper: PlaceWrapper): InputArcWrapper {
        return inputArcs.find { it.fromPlace == placeWrapper }!!
    }

    override fun compareTo(other: TransitionWrapper): Int {
        return comparator.compare(this, other)
    }

    fun transitionMustBeLogged(): Boolean {
        return this in model.loggedTransitions
    }

    fun printIndependentArcGroups() {
        buildString {
            appendLine("independent arc groups:")
            buildString {
                for (group in independentMultiArcConditions) {
                    appendLine(group)
                }
            }.prependIndent().let { append(it) }
        }.let { println(it) }
    }

    val transitionHistory = TransitionHistory(transitionId)

    fun inputArcsSolutions(
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator,
        v2Solver: Boolean = true
    ): Iterable<FullSolution> {
        return when (model.tokensAreEntities()) {
            true -> {
                if (v2Solver) {
                    TransitionSyncV2FullSolutionFinder(this, shuffler, tokenGenerator)
                        .asIterable(
                            tokenSlice,
                            (solutionCache.solution as? FullSolution.DoesNotExistSynchronized)?.tokenSet
                        )
                } else {
                    TransitionSynchronizationArcSolver(this)
                        .getSolutionFinderIterable(
                            tokenSlice,
                            shuffler,
                            tokenGenerator = tokenGenerator,
                        )
                        ?: emptyList()
                }
            }

            false -> {
                SimpleArcSolver(tokenSlice, this)
            }
        }
    }

    private fun nonDeterministicDistributeTokensOverArcs(
        snapshot: TokenArcFlowSnapshotFactory.Snapshot,
        shuffler: Shuffler
    ): MutableMap<OutputArcWrapper, MutableList<TokenWrapper>> {
        val typeToArcs = mutableMapOf<ObjectType, MutableList<OutputArcWrapper>>()
        outputArcs.forEach {
            typeToArcs.getOrPut(it.objectType) { mutableListOf() }.add(it)
        }

        // which tokens to generate and how much?
        val requiredAmountPerArc = outputArcs.associateBy({ it }) {
            it.getAmountToProduce(snapshot)
        }

        val arcToTokens = mutableMapOf<OutputArcWrapper, MutableList<TokenWrapper>>()

        for ((objectType, arcs) in typeToArcs) {
            val objectTypeTokens =
                snapshot.getGroup(objectType)
                    ?.tokens
                    ?.let {
                        it.buildFromIndices(shuffler.makeShuffled(it.indices))
                    }
                    ?.toMutableList()
                    ?: emptyList()

            val stackPerArc = List(arcs.size) { mutableListOf<TokenWrapper>() }

            val iterator = objectTypeTokens.iterator()

            while (iterator.hasNext()) {
                // determine arcs that still need token consumption
                val writableArcIndices = arcs.mapIndexedNotNull { index, outputArcWrapper ->
                    if (stackPerArc[index].size == requiredAmountPerArc[outputArcWrapper]!!) {
                        null
                    } else {
                        index
                    }
                }
                if (writableArcIndices.isEmpty()) {
                    break;
                }
                val consumer = shuffler.select(writableArcIndices)
                stackPerArc[consumer].add(iterator.next())
            }

            arcs.zip(stackPerArc) { arc, tokens ->
                arcToTokens[arc] = tokens.toMutableList()
            }
        }
        return arcToTokens
    }

    private fun simpleAmountsSolution(
        tokenSlice: TokenSlice,
    ): TokenSlice {
        val snapshot: TokenArcFlowSnapshotFactory.Snapshot = TokenArcFlowSnapshotFactory(this, tokenSlice)
            .getGrouped(GroupingStrategy.ByType)

        val arcsToTokens = outputArcs.associateBy({ it }) {
            it.getAmountToProduce(snapshot)
        }
        return SimpleTokenSlice.build {
            for ((outputArc, tokens) in arcsToTokens) {
                addAmount(outputArc.toPlace, tokens)
            }
        }
    }

    private fun tokenEntitiesOutputArcSolution(
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
    ): OutputArcsSolution {
        val snapshot: TokenArcFlowSnapshotFactory.Snapshot = TokenArcFlowSnapshotFactory(this, tokenSlice)
            .getGrouped(GroupingStrategy.ByType)

        // which tokens to generate and how much?
        val requiredAmountPerArc = outputArcs.associateBy({ it }) {
            it.getAmountToProduce(snapshot)
        }

        val tokensDistribution = nonDeterministicDistributeTokensOverArcs(snapshot, shuffler)

        val distributedTokens = tokensDistribution.values.flatten().toSet()
        val deletedTokens = snapshot.allTokens.toSet().minus(distributedTokens).toList()

        val generatedTokens = mutableListOf<TokenWrapper>()
        outputArcs.forEach {
            val requiredAmount = requiredAmountPerArc[it]!!
            val alreadyHave = tokensDistribution[it]!!.size

            val tokensToGenerate = (requiredAmount - alreadyHave).coerceAtLeast(0)
            for (i in 0..<tokensToGenerate) {
                val newToken = tokenGenerator.generateRealToken(it.objectType)
                generatedTokens.add(newToken)
                tokensDistribution[it]!!.add(newToken)
            }
        }

        val outputTokens = SimpleTokenSlice.build {
            for ((outputArc, tokens) in tokensDistribution) {
                addTokens(outputArc.toPlace, tokens)
            }
        }
        return OutputArcsSolution(plusTokens = outputTokens, deletedTokens, generatedTokens)
    }

    data class OutputArcsSolution(
        val plusTokens: TokenSlice,
        val consumedTokens: List<TokenWrapper>,
        val generatedTokens: List<TokenWrapper>
    ) {

    }

    fun outputArcsSolutions(
        tokenSlice: TokenSlice,
        shuffler: Shuffler,
        tokenGenerator: TokenGenerator
    ): OutputArcsSolution {
        return if (model.tokensAreEntities()) {
            tokenEntitiesOutputArcSolution(tokenSlice, shuffler, tokenGenerator)
        } else {
            OutputArcsSolution(simpleAmountsSolution(tokenSlice), emptyList(), emptyList())
        }
    }

    override val id: String
        get() = transitionId

    val prePlaces by lazy {
        model.placesRef.ref.selectIn(prePlacesIds).let { Places(it.sortedBy { it.placeId }) }
    }
    val prePlacesIds by lazy {
        getTransitionPreplacesMap(model.ocNet)[transitionId]!!.toSortedSet()
    }
    val postPlaces by lazy {
        model.placesRef.ref.selectIn(postPlacesIds).let { Places(it.sortedBy { it.placeId }) }
    }
    val postPlacesIds by lazy {
        getTransitionPostPlaces(model.ocNet)[transitionId]!!.toSortedSet()
    }
    val transitionsWithSharedPreplaces by lazy {
        model.transitionsRef.ref.transitions.selectIn(transitionsWithSharedPreplacesIds)
            .let { Transitions(it).sortedBy { it.transitionId } }
    }
    val transitionsWithSharedPreplacesIds by lazy {
        getTransitionsWithSharedPreplacesFor(model.ocNet, transitionId).sortedBy { it }
    }
    val dependentTransitionsIds by lazy {
        getDependentTransitions(model.ocNet, transitionId).sorted()
    }

    fun inputArcBy(placeId: PetriAtomId): InputArcWrapper {
        return inputArcs.find { it.fromPlace.id == placeId }!!
    }

    val inputArcConditions by lazy {
        inputArcs.flatMap {
            it.underConditions
        }
    }

    val independentMultiArcConditions: List<IndependentMultiConditionGroup> by lazy {
        inputArcs.mapNotNull { it.independentGroup }.toSortedSet().toList()
    }

    val unconditionalInputArcs by lazy {
        inputArcs.filter { it.underConditions.isEmpty() }.sorted()
    }

    val isolatedInputArcCombinations by lazy {
        for (arc in inputArcs) {
            val allAssociatedArcs = arc.underConditions.flatMap { it.arcs.ref }.toSet()
            val allArcAssociatedConditions = allAssociatedArcs.flatMap {
                it.underConditions
            }.toMutableSet()

        }
    }

    val inputArcsSortedByRequiredTokens by lazy {
        inputArcs.sortedBy { it.consumptionSpec }
    }

    val lomazovaVariabilityArcs by lazy {
        inputArcs.filter { it.arcMeta is LomazovaVariableArcMeta }
    }

    val aalstVariabilityArcs by lazy {
        inputArcs.filter { it.arcMeta is AalstVariableArcMeta }
    }

    val variables by lazy {
        lomazovaVariabilityArcs.map { it.arcMeta.cast<LomazovaVariableArcMeta>().variableName }.toSet()
    }

    val lomazovaVariableArcsToDependent by lazy {
        val variableDefinerArcs = lomazovaVariabilityArcs.filter {
            it.consumptionSpec is InputArcWrapper.ConsumptionSpec.Variable
        }

        val variableCreationArcToDependents = mutableMapOf<InputArcWrapper, MutableSet<InputArcWrapper>>()

        for (varDefiningArc in variableDefinerArcs) {
            val variable = varDefiningArc.consumptionSpec.castVar().variableName
            for (inputArc in inputArcs) {
                inputArc.consumptionSpec.castDependent()?.let {
                    if (it.variableName == variable) {
                        variableCreationArcToDependents.getOrPut(varDefiningArc) { mutableSetOf() }.add(inputArc)
                    }
                }
            }
            variableCreationArcToDependents.getOrPut(varDefiningArc) { mutableSetOf() }
        }

        variableCreationArcToDependents
    }

    val inputArcs by lazy {
        val syncGroups = getSyncGroups(model.simulationInput, transitionId)
            ?.sortedBy { it.arcsFromPlaces.size }


        val allocatedArcGroups = mutableMapOf<Int, MultiArcCondition>()

        // multi arc unique for transition by its index
        val syncGroupsSorted =
            syncGroups?.sortedBy { it.syncTransition }

        val inputArcWrappers = prePlaces.map { preplace ->
            val participatingSyncGroups =
                syncGroupsSorted?.mapIndexedNotNull { index, synchronizedArcGroup ->
                    if (preplace.id in synchronizedArcGroup.arcsFromPlaces) {
                        Pair(index, synchronizedArcGroup)
                    } else {
                        null
                    }
                }

            val createdSyncGroups = participatingSyncGroups?.map { (index, group) ->
                allocatedArcGroups.getOrPut(index) {
                    MultiArcCondition(
                        syncTarget = model.transitionsRef.ref.map[group.syncTransition]!!,
                        index = index,
                        arcs = Ref(),
                        transitionWrapper = this,
                        originalCondition = group
                    )
                }
            }
            InputArcWrapper(
                fromPlace = preplace,
                transition = this,
                model,
                underConditions = createdSyncGroups ?: listOf(),
                _independentGroup = Ref()
            )
        }
        allocatedArcGroups.values.forEach { condition ->
            condition.arcs.setRef(inputArcWrappers.filter { condition in it.underConditions }.sorted())
        }

        val conditionsByArcs = inputArcWrappers.map { arc ->
            Pair(arc, arc.underConditions.toMutableSet())
        }.fold(mutableSetOf<MutableSet<MultiArcCondition>>()) { acc, (arc, conditions) ->
            if (acc.isEmpty()) {
                acc.add(conditions)
            } else {
                for (i in acc) {
                    if (i.intersect(conditions).isNotEmpty()) {
                        i.addAll(conditions)
                        return@fold acc
                    }
                }
                acc.add(conditions)
            }
            acc
        }

        val independentGroups = conditionsByArcs.map {
            IndependentMultiConditionGroup(
                conditions = it.toSortedSet(),
                transition = this
            )
        }

        for (indepentGroup in independentGroups) {
            for (arc in indepentGroup.conditions.flatMap { it.arcs.ref }) {
                arc._independentGroup.setRef(indepentGroup)
            }
        }

        inputArcWrappers
    }

    val outputArcs by lazy {
        postPlaces.places.map {
            OutputArcWrapper(toPlace = it, transition = this, model = model)
        }
    }

    val dependentTransitions by lazy {
        model.transitionsRef.ref.transitions.mapNotNull { wrapper ->
            val entry = dependentTransitionsIds.find { wrapper.id == it.transition }

            if (entry != null) {
                wrapper
            } else {
                null
            }
        }
    }

    fun logTokensOnFireIfSynchronized(tokenSlice: TokenSlice) {
        if (model.tokensAreEntities() && this in model.loggedTransitions) {
            val newTransitionReference = getNewTransitionReference()

            for ((_, tokens) in tokenSlice.byPlaceIterator()) {
                for (token in tokens) {
                    addTokenVisit(newTransitionReference, token)
                }
            }
        }
    }

    fun getNewTransitionReference(): Long {
        return model.transitionIdIssuer.issueTransitionId()
    }

    fun checkEnabledByTokensCache(): Boolean {
        return solutionCache.solution != null && solutionCache.solution !is FullSolution.DoesNotExistSynchronized
    }

    fun addTokenVisit(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        tokenWrapper.recordTransitionVisit(transitionIndex, this)
        transitionHistory.recordReference(transitionIndex, tokenWrapper)
    }

    fun removeTokenVisit(tokenWrapper: TokenWrapper) {
        for (i in tokenWrapper.visitedTransitions) {
            i.transitionHistory.decrementReference(tokenWrapper)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransitionWrapper

        return transitionId == other.transitionId
    }

    override fun hashCode(): Int {
        return transitionId.hashCode()
    }

    override fun toString(): String {
        return "t($id)"
    }

    data class AffectedTransition(
        val transition: TransitionWrapper,
        val middlemanPlaces: Places
    )
}

class TransitionIdIssuer {
    var counter: Long = 0
        private set

    fun issueTransitionId(): Long {
        return counter++
    }
}

class TransitionHistory(val transitionId: PetriAtomId) {
    private val logIdToReferenceCounter = mutableMapOf<Long, Long>()
    private val idToAssociations = sortedMapOf<Long, MutableSet<TokenWrapper>>()
    private val _allIds = sortedSetOf<Long>()

    val allLogIndices: Set<Long> = _allIds

    fun size(): Int {
        return allLogIndices.size
    }

    fun entries(): Collection<Set<TokenWrapper>> {
        return idToAssociations.values
    }

    fun recordReference(transitionLogIndex: Long, tokenWrapper: TokenWrapper) {
//        val current = logIdToReferenceCounter.getOrPut(transitionLogIndex) {
//            _allIds.add(transitionLogIndex)
//            0
//        }
        idToAssociations.getOrPut(transitionLogIndex) {
            mutableSetOf()
        }.add(tokenWrapper)
//        logIdToReferenceCounter[transitionLogIndex] = current + 1
    }

    fun decrementReference(tokenWrapper: TokenWrapper) {
        var removed = false
        val emptySets by lazy(LazyThreadSafetyMode.NONE) { removed = true; mutableListOf<Long>() }
        for ((key, set) in idToAssociations) {
            set.remove(tokenWrapper)
            if (set.isEmpty()) {
                emptySets.add(key)
            }
        }
        if (removed) {
            for (set in emptySets) {
                idToAssociations.remove(set)
            }
        }
//        val newReferencesToLog = (logIdToReferenceCounter[transitionIndex]!! - 1).coerceAtLeast(0)
//        if (newReferencesToLog == 0L) {
//            logIdToReferenceCounter.remove(transitionIndex)
//            idToAssociations.remove(transitionIndex)
//            _allIds.remove(transitionIndex)
//        } else {
//            logIdToReferenceCounter[transitionIndex] = newReferencesToLog
//            idToAssociations[transitionIndex]!!.remove(tokenWrapper)
//        }
    }

    override fun toString(): String {
        return "TransitionHistory($transitionId, idToAssociations=$idToAssociations)"
    }
}

data class TransitionTimer(
    @TimePNRef("h(t)", comment = "Not exactly h(t), but tracks the clocks of transitions")
    var counter: Long,
    @TimePNRef("earliest firing time")
    var eft: Long = 0,
    @TimePNRef("longest firing time")
    var lft: Long = 0,
) {

    fun timeUntilLFT(): Long {
        return (lft - counter).coerceAtLeast(0)
    }

    fun timeUntilEFT(): Long {
        return (eft - counter).coerceAtLeast(0)
    }

    fun countup(units: Long) {
        counter = (counter + units).coerceAtLeast(lft)
    }

    fun updateBoundsAndReset(lft: Long, eft: Long) {
        require(eft <= lft)
        this.lft = lft
        this.eft = eft
        counter = 0
    }

    fun incrementCounter(delta: Long) {
        this.counter += delta
    }

    fun resetCounter() {
        counter = 0
    }

    fun mustFireNow(): Boolean {
        return counter >= lft
    }

    fun canFireNow(): Boolean {
        return counter >= eft
    }
}