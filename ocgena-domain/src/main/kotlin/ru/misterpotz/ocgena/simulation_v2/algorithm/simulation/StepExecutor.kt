package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.stepexecutor.SparseTokenBunch
import ru.misterpotz.ocgena.simulation_v2.entities_selection.getDependentTransitions
import ru.misterpotz.ocgena.simulation_v2.entities_selection.getTransitionPostPlaces
import ru.misterpotz.ocgena.simulation_v2.entities_selection.getTransitionPreplacesMap
import ru.misterpotz.ocgena.simulation_v2.entities_selection.getTransitionsWithSharedPreplacesFor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenStore
import ru.misterpotz.ocgena.utils.TimePNRef
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
    private val _visited = mutableSetOf<PetriAtomId>()
    val visitedTransitions: Set<PetriAtomId> = _visited

    fun recordVisit(transitionId: PetriAtomId) {
        _visited.add(transitionId)
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

class OCNetAccessor(val ocNet: OCNet) {
    val transitionsRef: Ref<Transitions> = Ref()
    val placesRef: Ref<Places> = Ref()
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
        if (objectType != other.objectType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = placeId.hashCode()
        result = 31 * result + objectType.hashCode()
        return result
    }


}

fun <T : Identifiable> List<T>.selectIn(iterable: Iterable<String>) = filter { it.id in iterable }

interface ArcChecker {
    fun checkEnoughTokens(
        tokenSlice: TokenSlice
    ): Boolean
}

interface TransitionSolutionsIndexFactory {
    fun create(transition: TransitionWrapper): TransitionSolutionsIndex
}

interface TransitionSolutionsIndex {
    fun availableSolutions(): Int
    fun reindexSolutions(affectedPlaces: Places)
}

interface ArcSolver {
    fun getIndexedSolutionsMeta(): Meta
    fun getSolution(index: Int): Solution?

    data class Meta(val solutionsAmount: Int)
    data class Solution(
        val tokensRemoved: TokenSlice,
        val tokensAppended: TokenSlice,
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


class TransitionWrapper(
    val transitionId: PetriAtomId,
    val timer: TransitionTimer = TransitionTimer(0),
    val oCNetAccessor: OCNetAccessor,
    val arcLogicsFactory: ArcLogicsFactory,
    val indexFactory: TransitionSolutionsIndexFactory,
) : Identifiable {
    override val id: String
        get() = transitionId

    val index by lazy {
        indexFactory.create(this)
    }

    val prePlaces by lazy {
        oCNetAccessor.placesRef.ref.selectIn(prePlacesIds).let { Places(it) }
    }
    val prePlacesIds by lazy {
        getTransitionPreplacesMap(oCNetAccessor.ocNet)[transitionId]!!
    }
    val postPlaces by lazy {
        oCNetAccessor.placesRef.ref.selectIn(postPlacesIds).let { Places(it) }
    }
    val postPlacesIds by lazy {
        getTransitionPostPlaces(oCNetAccessor.ocNet)[transitionId]!!
    }
    val transitionsWithSharedPreplaces by lazy {
        oCNetAccessor.transitionsRef.ref.selectIn(transitionsWithSharedPreplacesIds).let { Transitions(it) }
    }
    val transitionsWithSharedPreplacesIds by lazy {
        getTransitionsWithSharedPreplacesFor(oCNetAccessor.ocNet, transitionId)
    }

    val dependentTransitionsIds by lazy {
        getDependentTransitions(oCNetAccessor.ocNet, transitionId)
    }

    val dependentTransitions by lazy {
        oCNetAccessor.transitionsRef.ref.transitions.mapNotNull { wrapper ->
            val entry = dependentTransitionsIds.find { wrapper.id == it.transition }

            if (entry != null) {
                AffectedTransition(
                    wrapper,
                    oCNetAccessor.placesRef.ref
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
            ocNet = oCNetAccessor.ocNet,
            prePlaces = prePlaces,
            toTransition = this
        )
    }

    val arcSolver by lazy {
        arcLogicsFactory.getArcSolver(oCNetAccessor.ocNet, prePlaces, toTransition = this)
    }

    fun enabledByTokens(tokenSlice: TokenSlice): Boolean {
        return arcChecker.checkEnoughTokens(tokenSlice)
    }

    fun getSolutions(tokenSlice: TokenSlice): ArcSolver.Meta {
        return arcSolver.getIndexedSolutionsMeta()
    }

    fun reindexSolutions(affectedPlaces: Places) {
        index.reindexSolutions(affectedPlaces)
    }

    fun getFiringSolution(index: Int): ArcSolver.Solution? {
        return arcSolver.getSolution(index)
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


class StepExecutor(
    val transitions: Transitions,
    val places: Places,
    val shiftTimeSelector: ShiftTimeSelector,
    val transitionSelector: TransitionSelector,
    val transitionSolutionSelector: TransitionSolutionSelector,
    val tokenStore: TokenStore,
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

    private fun cleanGarbageTokens() {
        // need to clean tokens here from all caches and records and indeces
    }

    private suspend fun fireTransition(transition: TransitionWrapper) {
        val solutionsMeta = transition.getSolutions(tokenStore)
        val chosenSolution = transitionSolutionSelector.get(solutionsMeta)
        val solution = transition.getFiringSolution(chosenSolution)

        requireNotNull(solution)

        tokenStore.minus(solution.tokensRemoved)
        tokenStore.plus(solution.tokensAppended)

        transition.dependentTransitions.forEach {
            it.transition.reindexSolutions(it.middlemanPlaces)
        }
        transition.transitionsWithSharedPreplaces.forEach { t ->
            t.timer.resetCounter()
        }
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