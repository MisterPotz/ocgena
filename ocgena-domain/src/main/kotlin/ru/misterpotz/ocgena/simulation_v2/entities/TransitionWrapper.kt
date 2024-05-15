package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.*
import ru.misterpotz.ocgena.simulation_v2.algorithm.solution_search.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.*
import ru.misterpotz.ocgena.simulation_v2.entities_storage.*
import ru.misterpotz.ocgena.simulation_v2.utils.Identifiable
import ru.misterpotz.ocgena.simulation_v2.utils.Ref
import ru.misterpotz.ocgena.simulation_v2.utils.selectIn
import ru.misterpotz.ocgena.utils.TimePNRef
import ru.misterpotz.ocgena.utils.cast

class CheckingCache {
    var isEnabledByMarking: Boolean = false
    var needCheckCache: Boolean = true
        set(value) {
            if (value) {
                isEnabledByMarking = false
            }
            field = value
        }
}

fun InputArcWrapper.ConsumptionSpec.castVar(): InputArcWrapper.ConsumptionSpec.Variable {
    return this as InputArcWrapper.ConsumptionSpec.Variable
}

fun InputArcWrapper.ConsumptionSpec.castDependent(): InputArcWrapper.ConsumptionSpec.DependsOnVariable? {
    return this as? InputArcWrapper.ConsumptionSpec.DependsOnVariable?
}

class TransitionWrapper(
    val transitionId: PetriAtomId,
    val model: ModelAccessor,
    val arcLogicsFactory: ArcLogicsFactory = ArcLogicsFactory.Stub,
    val timer: TransitionTimer = TransitionTimer(0),
) : Identifiable, Comparable<TransitionWrapper> {
    val checkingCache = CheckingCache()

    fun setNeedCheckCache() {
        checkingCache.needCheckCache = true
    }

    fun setEnabledByMarkingCache(enabledByMarking: Boolean) {
        checkingCache.needCheckCache = false
        checkingCache.isEnabledByMarking = enabledByMarking
    }

    fun needCheckCache(): Boolean {
        return checkingCache.needCheckCache
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

    val transitionHistory = TransitionHistory()

    fun inputArcsSolutions(
        tokenSlice: TokenSlice,
        shuffler: Shuffler
    ): Iterable<FullSolution> {
        return when (model.tokensAreEntities()) {
            true -> {
                TransitionSynchronizationArcSolver(this)
                    .getSolutionFinderIterable(tokenSlice, shuffler)
                    ?: emptyList()
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
                    .tokens
                    .let {
                        it.buildFromIndices(shuffler.makeShuffled(it.indices))
                    }
                    .toMutableList()

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

        outputArcs.forEach {
            val requiredAmount = requiredAmountPerArc[it]!!
            val alreadyHave = tokensDistribution[it]!!.size

            val tokensToGenerate = (requiredAmount - alreadyHave).coerceAtLeast(0)
            for (i in 0..<tokensToGenerate) {
                val newToken = tokenGenerator.generateRealToken(it.objectType)
                tokensDistribution[it]!!.add(newToken)
            }
        }

        val outputTokens = SimpleTokenSlice.build {
            for ((outputArc, tokens) in tokensDistribution) {
                addTokens(outputArc.toPlace, tokens)
            }
        }
        return OutputArcsSolution(plusTokens = outputTokens, deletedTokens)
    }

    data class OutputArcsSolution(
        val plusTokens: TokenSlice,
        val consumedTokens: List<TokenWrapper>
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
            OutputArcsSolution(simpleAmountsSolution(tokenSlice), emptyList())
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
            condition.arcs._ref =
                inputArcWrappers.filter { condition in it.underConditions }.sorted()
        }

        val groups = mutableSetOf<IndependentMultiConditionGroup>()

        for (i in inputArcWrappers) {
            if (i.underConditions.isNotEmpty()) {
                groups.add(
                    IndependentMultiConditionGroup(
                        conditions = i.allAssociatedConditions,
                        transition = this
                    )
                )
            }
        }

        for (i in groups) {
            for (arc in inputArcWrappers) {
                if (arc in i.relatedInputArcs) {
                    arc._independentGroup._ref = i
                }
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

    // 3 modes 3 modes 3 modes
    val arcChecker by lazy {
        arcLogicsFactory.getArcChecker(
            ocNet = model.ocNet,
            prePlaces = prePlaces,
            toTransition = this
        )
    }

    val arcSolver by lazy {
        arcLogicsFactory.getArcSolver(model.ocNet, prePlaces, toTransition = this)
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

    fun enabledByTokens(tokenSlice: TokenSlice): Boolean {
        return arcChecker.checkEnoughTokens(tokenSlice)
    }

    fun checkEnabledByTokensCache(): Boolean {
        return checkingCache.isEnabledByMarking
    }

    fun addTokenVisit(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        tokenWrapper.recordTransitionVisit(transitionIndex, this)
        transitionHistory.recordReference(transitionIndex, tokenWrapper)
    }

    fun removeTokenVisit(tokenWrapper: TokenWrapper) {
        val intersectedIndices = tokenWrapper.participatedTransitionIndices[this]
            ?.intersect(transitionHistory.allLogIndices) ?: emptySet()

        for (i in intersectedIndices) {
            transitionHistory.decrementReference(i, tokenWrapper)
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

class TransitionHistory {
    private val logIdToReferenceCounter = mutableMapOf<Long, Long>()
    private val idToAssociations = mutableMapOf<Long, MutableSet<TokenWrapper>>()
    private val _allIds = sortedSetOf<Long>()

    val allLogIndices: Set<Long> = _allIds

    fun size(): Int {
        return allLogIndices.size
    }

    fun recordReference(transitionLogIndex: Long, tokenWrapper: TokenWrapper) {
        val current = logIdToReferenceCounter.getOrPut(transitionLogIndex) {
            _allIds.add(transitionLogIndex)
            0
        }
        idToAssociations.getOrPut(transitionLogIndex) {
            mutableSetOf()
        }.add(tokenWrapper)
        logIdToReferenceCounter[transitionLogIndex] = current + 1
    }

    fun decrementReference(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        val newReferencesToLog = (logIdToReferenceCounter[transitionIndex]!! - 1).coerceAtLeast(0)
        if (newReferencesToLog == 0L) {
            logIdToReferenceCounter.remove(transitionIndex)
            idToAssociations.remove(transitionIndex)
            _allIds.remove(transitionIndex)
        } else {
            logIdToReferenceCounter[transitionIndex] = newReferencesToLog
            idToAssociations[transitionIndex]!!.remove(tokenWrapper)
        }
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