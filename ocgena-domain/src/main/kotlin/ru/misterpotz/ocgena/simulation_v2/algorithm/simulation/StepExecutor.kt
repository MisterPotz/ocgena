package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation_v2.entities_selection.*
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SortedTokens
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.utils.TimePNRef
import java.util.SortedSet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Transitions(val transitions: List<TransitionWrapper>) : List<TransitionWrapper> by transitions {
    val map by lazy(LazyThreadSafetyMode.NONE) {
        transitions.associateBy { it.transitionId }
    }
}

fun List<TransitionWrapper>.wrap() = Transitions(this)
fun List<PlaceWrapper>.wrap() = Places(this)

class TokenWrapper(
    val tokenId: String,
    val objectType: ObjectType
) {
    private val _visited = mutableSetOf<TransitionWrapper>()
    val visitedTransitions: Set<TransitionWrapper> = _visited

    private val _participatedTransitionIndices = sortedSetOf<Long>()
    val participatedTransitionIndices: SortedSet<Long> = _participatedTransitionIndices

    fun recordTransitionVisit(transitionIndex: Long, transitionWrapper: TransitionWrapper) {
        _visited.add(transitionWrapper)
        _participatedTransitionIndices.add(transitionIndex)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenWrapper

        if (tokenId != other.tokenId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tokenId.hashCode()
        return result
    }
}

class Places(val places: List<PlaceWrapper>) : List<PlaceWrapper> by places {
    val map = places.associateBy { it.placeId }
}

interface ShiftTimeSelector {
    suspend fun get(timeRange: LongRange): Long
}

interface TransitionSelector {
    suspend fun get(transitions: Transitions): TransitionWrapper
}

interface TransitionSolutionSelector {
    suspend fun get(solutionMeta: ArcSolver.Meta): Int
}

class SimulationStateAccessor(
    val ocNet: OCNet,
    val simulationInput: SimulationInput
) {
    val transitionsRef: Ref<Transitions> = Ref()
    val placesRef: Ref<Places> = Ref()

    val transitionIdIssuer = TransitionIdIssuer()
    val outPlaces: Places by lazy {
        placesRef.ref.places.selectIn(ocNet.outputPlaces.iterable).wrap()
    }
}

class Ref<T>() {
    var _ref: T? = null

    val ref: T
        get() = _ref!!
}

interface Identifiable {
    val id: String
}

class PlaceWrapper(
    val placeId: PetriAtomId,
    val objectType: ObjectTypeId,
) : Identifiable {
    override val id: String
        get() = placeId

    fun getTokensAmount(tokenBunch: SparseTokenBunch): Int {
        return tokenBunch.tokenAmountStorage().getTokensAt(placeId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceWrapper

        if (placeId != other.placeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = placeId.hashCode()
        return result
    }
}

fun <T : Identifiable> List<T>.selectIn(iterable: Iterable<String>) = filter { it.id in iterable }

interface ArcChecker {
    fun checkEnoughTokens(
        tokenSlice: TokenSlice
    ): Boolean
}

interface ArcSolver {
    fun getIndexedSolutionsMeta(): Meta
    fun getSolution(index: Int): Solution?

    data class Meta(val solutionsAmount: Int)
    data class Solution(
        val tokensRemoved: TokenSlice,
        val tokensAppended: TokenSlice,
        val garbagedTokens: SortedTokens
    )
}

interface ArcLogicsFactory {

    fun getArcChecker(
        ocNet: OCNet,
        prePlaces: Places,
        toTransition: TransitionWrapper
    ): ArcChecker

    fun getArcSolver(
        ocNet: OCNet,
        prePlaces: Places,
        toTransition: TransitionWrapper,
    ): ArcSolver
}

//class ArcCondition(
//    val synchronizingTransition: TransitionWrapper,
//    val arcsInCondition: List<>
//) {
//
//}


class SynchronizedArcGroupCondition(
    val syncTarget: TransitionWrapper,
    val index: Int,
    val arcs: Ref<List<InputArcWrapper>>,
    val transitionWrapper: TransitionWrapper,
) {

    val arcWithStrongestCondition by lazy {
        arcs.ref.maxBy { it.underConditions.size }
    }

    fun check(tokenSlice: TokenSlice) {
        // lets see how much they should it

        arcs.ref.map {
            it.
        }

    }

    fun synchronizeSolutionsFromArcs() {
        val currentApplicableTokensSorted = arcs.ref.filter { it.currentSolutionSeachFilteredTokens != null }
            .sortedBy { it.currentSolutionSeachFilteredTokens!!.size }

        currentApplicableTokensSorted.forEach {
            it.currentSolutionSeachFilteredTokens!!.forEach {
                it.participatedTransitionIndices.intersect(arcs.)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SynchronizedArcGroupCondition

        if (syncTarget != other.syncTarget) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = syncTarget.hashCode()
        result = 31 * result + index
        return result
    }
}

class InputArcWrapper(
    val fromPlace: PlaceWrapper,
    val transition: TransitionWrapper,
    val simulationStateAccessor: SimulationStateAccessor,
    val underConditions: List<SynchronizedArcGroupCondition>
) {
    val syncTransitions by lazy {
        buildSet {
            underConditions.forEach {
                add(it.syncTarget)
            }
        }
    }
    val arc by lazy {
        simulationStateAccessor.ocNet.arcsRegistry.withArrow(transition.id).from(fromPlace.placeId)
    }

    val arcMeta: ArcMeta by lazy {
        arc.arcMeta
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

    sealed interface ConsumptionSpec {

        fun complies(amount: Int): Boolean {
            return when (this) {
                AtLeastOne -> amount > 0
                is Exact -> amount >= number
            }
        }

        data class Exact(val number: Int) : ConsumptionSpec
        data object AtLeastOne : ConsumptionSpec
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

class TransitionWrapper(
    val transitionId: PetriAtomId,
    val timer: TransitionTimer = TransitionTimer(0),
    val simulationStateAccessor: SimulationStateAccessor,
    val arcLogicsFactory: ArcLogicsFactory,
) : Identifiable {

    val transitionHistory = TransitionHistory()

    override val id: String
        get() = transitionId

    val prePlaces by lazy {
        simulationStateAccessor.placesRef.ref.selectIn(prePlacesIds).let { Places(it) }
    }
    val prePlacesIds by lazy {
        getTransitionPreplacesMap(simulationStateAccessor.ocNet)[transitionId]!!
    }
    val postPlaces by lazy {
        simulationStateAccessor.placesRef.ref.selectIn(postPlacesIds).let { Places(it) }
    }
    val postPlacesIds by lazy {
        getTransitionPostPlaces(simulationStateAccessor.ocNet)[transitionId]!!
    }
    val transitionsWithSharedPreplaces by lazy {
        simulationStateAccessor.transitionsRef.ref.selectIn(transitionsWithSharedPreplacesIds).let { Transitions(it) }
    }
    val transitionsWithSharedPreplacesIds by lazy {
        getTransitionsWithSharedPreplacesFor(simulationStateAccessor.ocNet, transitionId)
    }
    val dependentTransitionsIds by lazy {
        getDependentTransitions(simulationStateAccessor.ocNet, transitionId)
    }

    val inputArcConditions by lazy {
        inputArcs.flatMap {
            it.underConditions
        }
    }

    val inputArcs by lazy {
        val syncGroups = getSyncGroups(simulationStateAccessor.simulationInput, transitionId)
        val allocatedArcGroups = mutableMapOf<Int, SynchronizedArcGroupCondition>()

        val inputArcWrappers = prePlaces.map { preplace ->
            val participatingSyncGroups =
                syncGroups?.mapIndexedNotNull { index, synchronizedArcGroup ->
                    if (preplace.id in synchronizedArcGroup.arcsFromPlaces) {
                        Pair(index, synchronizedArcGroup)
                    } else {
                        null
                    }
                }

            val createdSyncGroups = participatingSyncGroups?.map { (index, group) ->
                allocatedArcGroups.getOrPut(index) {
                    SynchronizedArcGroupCondition(
                        syncTarget = simulationStateAccessor.transitionsRef.ref.map[group.syncTransition]!!,
                        index = index,
                        arcs = Ref(),
                        transitionWrapper = this
                    )
                }
            }
            InputArcWrapper(
                fromPlace = preplace,
                transition = this,
                simulationStateAccessor,
                underConditions = createdSyncGroups ?: listOf()
            )
        }
        allocatedArcGroups.values.forEach { condition ->
            condition.arcs._ref = inputArcWrappers.filter { condition in it.underConditions }
        }

        inputArcWrappers
    }

    val dependentTransitions by lazy {
        simulationStateAccessor.transitionsRef.ref.transitions.mapNotNull { wrapper ->
            val entry = dependentTransitionsIds.find { wrapper.id == it.transition }

            if (entry != null) {
                AffectedTransition(
                    wrapper,
                    simulationStateAccessor.placesRef.ref
                        .selectIn(entry.middlemanPlaces)
                        .wrap()
                )
            } else {
                null
            }
        }
    }

    // 3 modes 3 modes 3 modes
    val arcChecker by lazy {
        arcLogicsFactory.getArcChecker(
            ocNet = simulationStateAccessor.ocNet,
            prePlaces = prePlaces,
            toTransition = this
        )
    }

    val arcSolver by lazy {
        arcLogicsFactory.getArcSolver(simulationStateAccessor.ocNet, prePlaces, toTransition = this)
    }

    fun getNewTransitionReference(): Long {
        return simulationStateAccessor.transitionIdIssuer.issueTransitionId()
    }

    fun enabledByTokens(tokenSlice: TokenSlice): Boolean {
        return arcChecker.checkEnoughTokens(tokenSlice)
    }

    fun getSolutions(tokenSlice: TokenSlice): ArcSolver.Meta {
        return arcSolver.getIndexedSolutionsMeta()
    }

    fun getFiringSolution(index: Int): ArcSolver.Solution? {
        return arcSolver.getSolution(index)
    }

    fun addTokenVisit(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        tokenWrapper.recordTransitionVisit(transitionIndex, this)
        transitionHistory.recordReference(transitionIndex, tokenWrapper)
    }

    fun removeTokenVisit(tokenWrapper: TokenWrapper) {
        val intersectedIndices = tokenWrapper.participatedTransitionIndices
            .intersect(transitionHistory.allIds)

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

    data class AffectedTransition(
        val transition: TransitionWrapper,
        val middlemanPlaces: Places
    )
}

enum class EnabledMode {
    DISABLED_BY_MARKING,
    ENABLED_BY_MARKING,
    CAN_FIRE,
}

class TransitionIdIssuer {
    var counter: Long = 0
        private set

    fun issueTransitionId(): Long {
        return counter++
    }
}

class TransitionHistory {
    private val idToReferenceCounter = mutableMapOf<Long, Long>()
    private val idToAssociations = mutableMapOf<Long, MutableSet<TokenWrapper>>()
    private val _allIds = sortedSetOf<Long>()

    val allIds: Set<Long> = _allIds

    fun recordReference(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        val current = idToReferenceCounter.getOrPut(transitionIndex) {
            _allIds.add(transitionIndex)
            0
        }
        idToAssociations.getOrPut(transitionIndex) {
            mutableSetOf()
        }.add(tokenWrapper)
        idToReferenceCounter[transitionIndex] = current + 1
    }

    fun decrementReference(transitionIndex: Long, tokenWrapper: TokenWrapper) {
        val new = (idToReferenceCounter[transitionIndex]!! - 1).coerceAtLeast(0)
        if (new == 0L) {
            idToReferenceCounter.remove(transitionIndex)
            idToAssociations.remove(transitionIndex)
            _allIds.remove(transitionIndex)
        } else {
            idToReferenceCounter[transitionIndex] = new
            idToAssociations[transitionIndex]!!.remove(tokenWrapper)
        }
    }
}

class TokenArcFlowSnapshot(
    val records: List<ConsumedPlaceRecord>
) {
    private val groupedByType by lazy(LazyThreadSafetyMode.NONE) {
        val map: MutableMap<SimpleGroupedRecords.CompoundKey, MutableList<ConsumedPlaceRecord>> = mutableMapOf()
        for (record in records) {
            val key = SimpleGroupedRecords.CompoundKey(arcMeta = null, objectType = record.objectType)

            map.getOrPut(key) {
                mutableListOf()
            }.add(record)
        }
        @Suppress("UNCHECKED_CAST")
        SimpleGroupedRecords(map as Map<Any, List<ConsumedPlaceRecord>>)
    }

    fun getGrouped(groupingStrategy: GroupingStrategy): GroupedRecords {
        return when (groupingStrategy) {
            GroupingStrategy.ByType -> groupedByType
        }
    }

    data class ConsumedPlaceRecord(
        val amount: Int,
        val tokens: List<TokenWrapper>?,
        val objectType: ObjectType,
        val place: PlaceWrapper,
        val arcMeta: ArcMeta
    )

    interface GroupedRecords {
        fun getRecords(
            objectType: ObjectType,
            arcMeta: ArcMeta
        ): List<ConsumedPlaceRecord>
    }

    enum class GroupingStrategy {
        ByType
    }

    private class SimpleGroupedRecords(
        val groupedRecords: Map<Any, List<ConsumedPlaceRecord>>
    ) : GroupedRecords {
        override fun getRecords(objectType: ObjectType, arcMeta: ArcMeta): List<ConsumedPlaceRecord> {
            val compoundKey = CompoundKey(arcMeta, objectType)

            return groupedRecords[compoundKey]!!
        }

        data class CompoundKey(val arcMeta: ArcMeta?, val objectType: ObjectType?)
    }
}

class StepExecutor(
    val transitions: Transitions,
    val places: Places,
    val shiftTimeSelector: ShiftTimeSelector,
    val transitionSelector: TransitionSelector,
    val transitionSolutionSelector: TransitionSolutionSelector,
    val tokenStore: TokenStore,
    val simulationStateAccessor: SimulationStateAccessor,
) {
    private fun collectTransitions(filter: EnabledMode, sourceTransitions: Transitions? = null): Transitions {
        return (sourceTransitions ?: transitions).filter {
            when (filter) {
                EnabledMode.CAN_FIRE -> {
                    it.timer.canFireNow() && it.enabledByTokens(tokenStore)
                }

                EnabledMode.DISABLED_BY_MARKING -> {
                    !it.enabledByTokens(tokenStore)
                }

                EnabledMode.ENABLED_BY_MARKING -> {
                    it.enabledByTokens(tokenStore)
                }
            }
        }.wrap()
    }

    @OptIn(ExperimentalContracts::class)
    private suspend inline fun <T> step(description: String, crossinline action: suspend () -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        return run {
            action()
        }
    }

    suspend fun execute() {
        val simulationFinish = step("try finding shift time and increment clocks") {
            // определяем какие transition enabled
            val enabledByMarkign = collectTransitions(EnabledMode.ENABLED_BY_MARKING)

            val shiftTimes = determineShiftTimes(enabledByMarkign) ?: return@step true

            val shiftTime = selectShiftTime(shiftTimes)
            enabledByMarkign.forEach { t -> t.timer.incrementCounter(shiftTime) }
            false
        }
        if (simulationFinish) return

        step("execute transition if possible") {
            val fireableTransitions = collectTransitions(EnabledMode.CAN_FIRE)
            val selectedTransition = selectTransitionToFire(fireableTransitions)

            fireTransition(selectedTransition)
        }

        step("reset clocks of transitions that became disabled") {
            collectTransitions(EnabledMode.DISABLED_BY_MARKING).forEach { t ->
                t.timer.resetCounter()
            }
        }

        step("clean tokens whose participation is finished") {
            cleanGarbageTokens()
        }
    }

    private fun cleanTokenTransitionVisits(token: TokenWrapper) {
        for (visitedTransition in token.visitedTransitions) {
            visitedTransition.removeTokenVisit(token)
        }
    }

    private fun cleanTokenTransitionVisits(tokens: SortedTokens) {
        for (token in tokens) {
            cleanTokenTransitionVisits(token)
        }
    }

    private fun cleanGarbageTokens() {
        simulationStateAccessor.outPlaces.forEach { endPlace ->
            tokenStore.tokensAt(endPlace).forEach { token ->
                cleanTokenTransitionVisits(token)
            }
            tokenStore.modifyTokensAt(endPlace) { tokens -> tokens.clear() }
        }
    }

    private suspend fun fireTransition(transition: TransitionWrapper) {
        val solutionsMeta = transition.getSolutions(tokenStore)
        val chosenSolution = transitionSolutionSelector.get(solutionsMeta)
        val solution = transition.getFiringSolution(chosenSolution)

        val reference = transition.getNewTransitionReference()

        requireNotNull(solution)

        tokenStore.minus(solution.tokensRemoved)
        tokenStore.plus(solution.tokensAppended)

        solution.tokensAppended.tokensIterator.forEach { tokenWrapper ->
            transition.addTokenVisit(reference, tokenWrapper)
        }

//        transition.dependentTransitions.forEach {
//            it.transition.reindexSolutions(it.middlemanPlaces)
//        }
        transition.transitionsWithSharedPreplaces.forEach { t ->
            t.timer.resetCounter()
        }

        cleanTokenTransitionVisits(solution.garbagedTokens)
    }

    private fun determineShiftTimes(enabledByMarking: Transitions): LongRange? {

        if (enabledByMarking.isEmpty()) return null

        // find time until min lft
        val minTimeUntilLftTrans = enabledByMarking.minByOrNull {
            it.timer.timeUntilLFT()
        }!!.timer.timeUntilLFT()
        // find time until min eft
        val minTimeUntilEftTrans = enabledByMarking.minByOrNull {
            it.timer.timeUntilEFT()
        }!!.timer.timeUntilEFT()

        return minTimeUntilEftTrans..minTimeUntilLftTrans
    }

    private suspend fun selectShiftTime(shiftTimes: LongRange): Long {
        return shiftTimeSelector.get(shiftTimes)
    }

    suspend fun selectTransitionToFire(transitions: Transitions): TransitionWrapper {
        return transitionSelector.get(transitions)
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