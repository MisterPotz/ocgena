package ru.misterpotz.ocgena.simulation_v2.entities

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.*
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.utils.Identifiable
import ru.misterpotz.ocgena.simulation_v2.utils.Ref
import ru.misterpotz.ocgena.simulation_v2.utils.selectIn
import ru.misterpotz.ocgena.utils.TimePNRef

class TransitionWrapper(
    val transitionId: PetriAtomId,
    val modelAccessor: ModelAccessor,
    val arcLogicsFactory: ArcLogicsFactory = ArcLogicsFactory.Stub,
    val timer: TransitionTimer = TransitionTimer(0),
) : Identifiable, Comparable<TransitionWrapper> {
    companion object {
        val comparator = compareBy<TransitionWrapper> {
            it.transitionId
        }
    }

    override fun compareTo(other: TransitionWrapper): Int {
        return comparator.compare(this, other)
    }

    val transitionHistory = TransitionHistory()

    override val id: String
        get() = transitionId

    val prePlaces by lazy {
        modelAccessor.placesRef.ref.selectIn(prePlacesIds).let { Places(it.sortedBy { it.placeId }) }
    }
    val prePlacesIds by lazy {
        getTransitionPreplacesMap(modelAccessor.ocNet)[transitionId]!!.toSortedSet()
    }
    val postPlaces by lazy {
        modelAccessor.placesRef.ref.selectIn(postPlacesIds).let { Places(it.sortedBy { it.placeId }) }
    }
    val postPlacesIds by lazy {
        getTransitionPostPlaces(modelAccessor.ocNet)[transitionId]!!.toSortedSet()
    }
    val transitionsWithSharedPreplaces by lazy {
        modelAccessor.transitionsRef.ref.transitions.selectIn(transitionsWithSharedPreplacesIds)
            .let { Transitions(it).sortedBy { it.transitionId } }
    }
    val transitionsWithSharedPreplacesIds by lazy {
        getTransitionsWithSharedPreplacesFor(modelAccessor.ocNet, transitionId).sortedBy { it }
    }
    val dependentTransitionsIds by lazy {
        getDependentTransitions(modelAccessor.ocNet, transitionId).sortedBy { it }
    }

    fun inputArcBy(placeId: PetriAtomId): InputArcWrapper {
        return inputArcs.find { it.fromPlace.id == placeId }!!
    }

    val inputArcConditions by lazy {
        inputArcs.flatMap {
            it.underConditions
        }
    }

    val intersectingMultiArcConditions: List<IntersectingMultiArcConditions> by lazy {
        val groups = mutableSetOf<IntersectingMultiArcConditions>()

        for (i in inputArcs) {
            groups.add(
                IntersectingMultiArcConditions(
                    conditions = i.allAssociatedConditions,
                    transition = this
                )
            )
        }

        groups.sorted()
    }

    val isolatedInputArcCombinations by lazy {
        for (arc in inputArcs) {
            val allAssociatedArcs = arc.underConditions.flatMap { it.arcs.ref }.toSet()
            val allArcAssociatedConditions = allAssociatedArcs.flatMap {
                it.underConditions
            }.toMutableSet()

        }
    }

    val inputArcs by lazy {
        val syncGroups = getSyncGroups(modelAccessor.simulationInput, transitionId)
            ?.sortedBy { it.arcsFromPlaces.size }

        val allocatedArcGroups = mutableMapOf<Int, MultiArcCondition>()

        val inputArcWrappers = prePlaces.map { preplace ->
            val participatingSyncGroups =
                syncGroups?.sortedBy { it.syncTransition }?.mapIndexedNotNull { index, synchronizedArcGroup ->
                    if (preplace.id in synchronizedArcGroup.arcsFromPlaces) {
                        Pair(index, synchronizedArcGroup)
                    } else {
                        null
                    }
                }

            val createdSyncGroups = participatingSyncGroups?.map { (index, group) ->
                allocatedArcGroups.getOrPut(index) {
                    MultiArcCondition(
                        syncTarget = modelAccessor.transitionsRef.ref.map[group.syncTransition]!!,
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
                modelAccessor,
                underConditions = createdSyncGroups ?: listOf()
            )
        }
        allocatedArcGroups.values.forEach { condition ->
            condition.arcs._ref =
                inputArcWrappers.filter { condition in it.underConditions }.sorted()
        }

        inputArcWrappers
    }

    val dependentTransitions by lazy {
        modelAccessor.transitionsRef.ref.transitions.mapNotNull { wrapper ->
            val entry = dependentTransitionsIds.find { wrapper.id == it.transition }

            if (entry != null) {
                AffectedTransition(
                    wrapper,
                    modelAccessor.placesRef.ref
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
            ocNet = modelAccessor.ocNet,
            prePlaces = prePlaces,
            toTransition = this
        )
    }

    val arcSolver by lazy {
        arcLogicsFactory.getArcSolver(modelAccessor.ocNet, prePlaces, toTransition = ths)
    }

    fun getNewTransitionReference(): Long {
        return modelAccessor.transitionIdIssuer.issueTransitionId()
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