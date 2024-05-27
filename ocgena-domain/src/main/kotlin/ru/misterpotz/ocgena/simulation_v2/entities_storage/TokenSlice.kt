package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Places
import ru.misterpotz.ocgena.simulation_v2.entities.ResolvedVariablesSpace
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.utils.PatternIdCreator
import ru.misterpotz.ocgena.utils.sortedInsert
import java.util.*

fun TokenSlice.resolveVariables(inputsArcs: Collection<InputArcWrapper>): ResolvedVariablesSpace {
    val variables = mutableMapOf<String, Int>()
    for (inputArc in inputsArcs) {
        val spec = inputArc.consumptionSpec
        if (spec is InputArcWrapper.ConsumptionSpec.Variable && relatedPlaces.contains(inputArc.fromPlace)) {
            val amount = amountAt(inputArc.fromPlace)
            variables[spec.variableName] = amount
        }
    }
    return ResolvedVariablesSpace(variables)
}

fun Map<PlaceWrapper, List<TokenWrapper>>.resolveVariables(inputArcs: Collection<InputArcWrapper>): ResolvedVariablesSpace {
    val variables = mutableMapOf<String, Int>()
    for (inputArc in inputArcs) {
        val spec = inputArc.consumptionSpec
        if (spec is InputArcWrapper.ConsumptionSpec.Variable) {
            val amount = this[inputArc.fromPlace]
            if (amount != null) {
                variables[spec.variableName] = amount.size
            }
        }
    }
    return ResolvedVariablesSpace(variables)
}


interface TokenSlice {
    val relatedPlaces: Set<PlaceWrapper>
    val tokensIterator: Iterator<TokenWrapper>

    fun withPlaceFilter(places: Places): TokenSlice
    fun setAmount(placeWrapper: PlaceWrapper, new: Int)

    //    fun modifyTokensAt(placeWrapper: PlaceWrapper, modifyBlock: (MutableSortedTokens) -> Unit)
    fun cleanTokensInPlace(placeWrapper: PlaceWrapper): SortedTokens
    fun amountAt(placeWrapper: PlaceWrapper): Int
    fun tokensAt(placeWrapper: PlaceWrapper): SortedTokens
    fun minusInPlace(tokenSlice: TokenSlice)
    fun plusInPlace(tokenSlice: TokenSlice)
    fun addTokensAt(place: PlaceWrapper, tokens: Collection<TokenWrapper>)
    fun addTokenAt(place: PlaceWrapper, token: TokenWrapper)

    //    fun filterTokensInPlaces(places: Places, predicate: (TokenWrapper, PlaceWrapper) -> Boolean): TokenSlice
    fun byPlaceIterator(): Iterator<Pair<PlaceWrapper, SortedTokens>>
    fun print()
    fun takeSnapshot(): String
    fun iterateByCommonHistoryEntries(exclude: Set<Long>, deepCopy: Boolean = false): Iterator<TokenSlice>
    fun deepCopy(): TokenSlice
}

interface HistoricalTokenSlice : TokenSlice {
    val historyEntry: Long
}

interface TokenGenerator {
    fun generateRealToken(type: ObjectType): TokenWrapper
    val issuedTokens: Map<Long, TokenWrapper>
}

class TokenStore(
    private val internalSlice: SimpleTokenSlice,
    override val issuedTokens: MutableMap<Long, TokenWrapper> = mutableMapOf(),
) : TokenSlice by internalSlice, TokenGenerator {
    private val tokenCreators = TokenCreators()

    override fun takeSnapshot(): String {
        return buildString {
            appendLine("tokenslice:")
            append(
                buildString {
                    appendLine(internalSlice.takeSnapshot())
                }.prependIndent()
            )
            appendLine("issued tokens:")
            append(
                buildString {
                    append(issuedTokens.toString())
                }.prependIndent()
            )
        }
    }

    private fun removeToken(tokenWrapper: TokenWrapper) {
        issuedTokens.remove(tokenWrapper.tokenId)
    }

    fun removeTokens(tokens: Collection<TokenWrapper>) {
//        println("token store removing tokens $tokens")
        for (token in tokens) {
            removeToken(token)
        }
    }

    override fun generateRealToken(type: ObjectType): TokenWrapper {
        return tokenCreators.create(type).also {
            issuedTokens[it.tokenId] = it
        }
    }

    fun minus(tokenSlice: TokenSlice) {
        internalSlice.minusInPlace(tokenSlice)
    }

    fun plus(tokenSlice: TokenSlice) {
        internalSlice.plusInPlace(tokenSlice)
    }

    override fun toString(): String {
        return "TokenStore(internalSlice=$internalSlice, " +
                "issuedTokens=${issuedTokens.map { it.value }})"
    }


}

class TokenCreators() {
    private val creators = mutableMapOf<ObjectType, SingleTypeTokenCreator>()
    private var index: Long = 0
    fun create(objectType: ObjectType): TokenWrapper {
        return creators.getOrPut(objectType) {
            SingleTypeTokenCreator(type = objectType, { index++ })
        }.makeNew()
    }
}

class SingleTypeTokenCreator(
    val type: ObjectType,
    private val nextIdProvider: () -> Long,
    private val idIssuer: PatternIdCreator = PatternIdCreator(nextIdProvider) {
        "${type.id}$it"
    },
) {
    fun makeNew(): TokenWrapper {
        return TokenWrapper(
            tokenId = idIssuer.newIntId,
            objectType = type,
        )
    }
}

typealias MutableSortedTokens = MutableList<TokenWrapper>
typealias SortedTokens = List<TokenWrapper>

data class HistoryEntriesData(
    var min: Long? = null,
    var max: Long? = null,
) {
    fun update(tokens: Collection<TokenWrapper>) {
        for (token in tokens) {
            compareAndSetMin(token)
        }
    }

    fun probablyIntersectsWith(historyEntriesData: HistoryEntriesData): Boolean {
        val min = min
        val max = max
        val otherMin = historyEntriesData.min
        val otherMax = historyEntriesData.max
        if (min == null || max == null || otherMin == null || otherMax == null) return false
        return (otherMin <= min && min <= otherMax) || (otherMin <= max && max <= otherMax)
    }

    private fun compareAndSetMin(token: TokenWrapper) {
        min.let {
            val thisMin = it
            val otherMin = token.tokenHistory.min
            if (thisMin == null) min = otherMin
            else if (otherMin != null && otherMin < thisMin) min = otherMin
        }
        max.let {
            val thisMax = it
            val otherMax = token.tokenHistory.max
            if (thisMax == null) max = otherMax
            else if (otherMax != null && thisMax < otherMax) max = otherMax
        }
    }
}

class SimpleTokenSlice(
    private val internalRelatedPlaces: SortedSet<PlaceWrapper>,
    private val tokensMap: SortedMap<PlaceWrapper, MutableSortedTokens> = sortedMapOf(),
    private val amountsMap: SortedMap<PlaceWrapper, Int> = sortedMapOf(),
) : TokenSlice {
    private val historyEntriesData = HistoryEntriesData()

    init {
        for (i in internalRelatedPlaces) {
            tokensMap.getOrPut(i) { mutableListOf() }
            amountsMap.getOrPut(i) { 0 }
        }
        for (tokens in tokensMap.values) {
            historyEntriesData.update(tokens)
        }
    }

    override val relatedPlaces: Set<PlaceWrapper>
        get() = internalRelatedPlaces
    override val tokensIterator: Iterator<TokenWrapper>
        get() = iterator {
            for (relatedPlace in internalRelatedPlaces) {
                val sortedTokens = tokensMap[relatedPlace]
                if (sortedTokens != null) {
                    yieldAll(sortedTokens)
                }
            }
        }

    fun tokenBy(id: Long): TokenWrapper {
        return tokensMap.values.find { it.find { it.tokenId == id } != null }!!.find { it.tokenId == id }!!
    }

    override fun withPlaceFilter(places: Places): TokenSlice {
        return SimpleTokenSlice(
            internalRelatedPlaces = internalRelatedPlaces.intersect(places).toSortedSet(),
            tokensMap.toSortedMap(),
            amountsMap.toSortedMap()
        )
    }

    override fun takeSnapshot(): String {
        return buildString {
            appendLine("tokensmap:")
            append(
                buildString {
                    val iterator = tokensMap.iterator()
                    while (iterator.hasNext()) {
                        val (place, tokens) = iterator.next()
                        append(
                            "${place.placeId} (size:${tokens.size}) ->  ${
                                tokens.joinToString(
                                    ",",
                                    prefix = "[",
                                    postfix = "]"
                                )
                            }"
                        )
                        if (iterator.hasNext()) {
                            appendLine()
                        }
                    }
                }.prependIndent()
            )
        }
    }

    private val bufferSlice by lazy(LazyThreadSafetyMode.NONE) {
        SimpleHistoricalTokenSlice(
            build {
                for (i in relatedPlaces) addRelatedPlace(i)
            },
            buffHistoryEntry = null
        )
    }

    fun clearTokens() {
        for ((_, tokens) in tokensMap) {
            tokens.clear()
        }
    }

    override fun deepCopy(): TokenSlice {
        return SimpleTokenSlice(
            internalRelatedPlaces.toSortedSet(),
            tokensMap.mapValues { it.value.toMutableList() }.toSortedMap(),
            amountsMap.mapValues { it.value }.toSortedMap()
        )
    }

    override fun iterateByCommonHistoryEntries(
        exclude: Set<Long>,
        deepCopy: Boolean,
    ): Iterator<TokenSlice> {
        return iterator {
            bufferSlice.clear()

            val placeToSize = tokensMap.iterator().asSequence().fold(mutableListOf<Int>()) { acc, (place, tokens) ->
                acc.add((acc.lastOrNull() ?: 0) + tokens.size)
                acc
            }
            val allTokens = tokensMap.values.flatten()

            fun placeForIndex(index: Int): PlaceWrapper {
                val placeIndex = placeToSize.indexOfFirst { index < it }
                for ((index, place) in tokensMap.keys.withIndex()) {
                    if (index == placeIndex) {
                        return place
                    }
                }
                throw IllegalStateException()
            }

            val tokensHistoryIterators = mutableListOf<Iterable<Long>>().apply {
                for ((_, tokens) in tokensMap.iterator()) {
                    for (token in tokens) {
                        add(token.tokenHistory)
                    }
                }
            }
            val parallelIteratorStack = ParallelIteratorStackChatV3(tokensHistoryIterators, exclusion = exclude)
            parallelIteratorStack.forEach { tokenEntriesCombination ->
                val historyEntry = tokenEntriesCombination.historyEntry
                bufferSlice.clear()

                bufferSlice.buffHistoryEntry = historyEntry
                for (index in tokenEntriesCombination.iterator) {
                    bufferSlice.addTokenAt(placeForIndex(index), allTokens[index])
                }
                val resulting = if (deepCopy) {
                    bufferSlice.copy()
                } else {
                    bufferSlice
                }
                yield(resulting)
            }
        }
    }

    private fun recalculateMinMaxHistoryEntries(tokens: Collection<TokenWrapper>) {
        historyEntriesData.update(tokens)
    }

    override fun print() {
        println(takeSnapshot())
    }

    override fun byPlaceIterator(): Iterator<Pair<PlaceWrapper, SortedTokens>> {
        return object : Iterator<Pair<PlaceWrapper, SortedTokens>> {
            val mapIterator = tokensMap.iterator()
            override fun hasNext(): Boolean {
                return mapIterator.hasNext()
            }

            override fun next(): Pair<PlaceWrapper, SortedTokens> {
                val next = mapIterator.next()
                return Pair(next.key, next.value)
            }
        }
    }

    override fun setAmount(placeWrapper: PlaceWrapper, new: Int) {
        if (placeWrapper !in relatedPlaces) return
        amountsMap[placeWrapper] = new
    }

    private fun addPlaceIfNeed(placeWrapper: PlaceWrapper) {
        tokensMap.getOrPut(placeWrapper) {
            mutableListOf()
        }
        amountsMap.getOrPut(placeWrapper) {
            0
        }
        internalRelatedPlaces.add(placeWrapper)
    }

    private fun modifyTokensAt(placeWrapper: PlaceWrapper, modifyBlock: (MutableSortedTokens) -> Unit) {
        if (placeWrapper !in relatedPlaces) return
        tokensMap[placeWrapper]!!.let(modifyBlock)
    }

    override fun cleanTokensInPlace(placeWrapper: PlaceWrapper): SortedTokens {
        if (placeWrapper !in relatedPlaces) return emptyList()
        val tokens = tokensMap[placeWrapper]!!
        tokens.clear()
        return tokens.toList()
    }

    override fun amountAt(placeWrapper: PlaceWrapper): Int {
        if (placeWrapper !in relatedPlaces) return 0

        return amountsMap[placeWrapper]!!
    }

    override fun tokensAt(placeWrapper: PlaceWrapper): SortedTokens {
        if (placeWrapper !in relatedPlaces) return mutableListOf()

        return tokensMap[placeWrapper]!!
    }

    override fun minusInPlace(tokenSlice: TokenSlice) {
        val commonPlaces = internalRelatedPlaces.intersect(tokenSlice.relatedPlaces)
        for (place in commonPlaces) {
            if (place !in relatedPlaces) {
                continue
            }
            modifyTokensAt(place) {
                it.removeAll(tokenSlice.tokensAt(place))
            }
            val totalPlace = amountAt(place) - tokenSlice.amountAt(place)
            require(totalPlace >= 0) {
                throw IllegalStateException("can't deduct from what doesn't exist")
            }
            setAmount(place, totalPlace)
        }
    }

    override fun plusInPlace(tokenSlice: TokenSlice) {
        for (place in tokenSlice.relatedPlaces) {
            addPlaceIfNeed(place)

            modifyTokensAt(place) { arr ->
                for (token in tokenSlice.tokensAt(place)) {
                    val indexOfInsertion = arr.indexOfFirst { it > token }
                    if (indexOfInsertion < 0) {
                        arr.add(token)
                    } else {
                        arr.add(indexOfInsertion, token)
                    }
                }
            }
            setAmount(place, amountAt(place) + tokenSlice.amountAt(place))
        }
    }

    override fun addTokensAt(place: PlaceWrapper, tokens: Collection<TokenWrapper>) {
        if (place !in relatedPlaces) return
        val placeTokens = tokensMap[place]!!

        sortedInsert(placeTokens, tokens)
        amountsMap[place] = amountsMap[place]!! + tokens.size
    }

    override fun addTokenAt(place: PlaceWrapper, token: TokenWrapper) {
        if (place !in relatedPlaces) return
        val placeTokens = tokensMap[place]!!

        sortedInsert(placeTokens, token)
        amountsMap[place] = amountsMap[place]!! + 1
    }

    override fun toString(): String {
        return buildString {
            append("TokenSlice($tokensMap, $amountsMap)")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleTokenSlice

        if (internalRelatedPlaces != other.internalRelatedPlaces) return false
        if (tokensMap != other.tokensMap) return false
        if (amountsMap != other.amountsMap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = internalRelatedPlaces.hashCode()
        result = 31 * result + tokensMap.hashCode()
        result = 31 * result + amountsMap.hashCode()
        return result
    }


    companion object {
        fun build(block: ConstructionBlock.() -> Unit): SimpleTokenSlice {
            val mutableMap = mutableMapOf<PlaceWrapper, SortedSet<TokenWrapper>>()
            val relatedPlaces = mutableListOf<PlaceWrapper>()
            val amounts = mutableMapOf<PlaceWrapper, Int>()

            val constructionBlock = object : ConstructionBlock {
                override fun addRelatedPlace(relatedPLace: PlaceWrapper) {
                    relatedPlaces.add(relatedPLace)
                }

                override fun addTokens(placeWrapper: PlaceWrapper, tokens: Collection<TokenWrapper>) {
                    mutableMap.getOrPut(placeWrapper) {
                        sortedSetOf()
                    }.addAll(tokens)
                }

                override fun addAmount(placeWrapper: PlaceWrapper, amount: Int) {
                    amounts[placeWrapper] = amount
                }
            }
            constructionBlock.block()
            return SimpleTokenSlice(
                internalRelatedPlaces = mutableMap.keys.toMutableSet().union(amounts.keys).union(relatedPlaces)
                    .toSortedSet(),
                tokensMap = mutableMap.mapValues { it.value.toMutableList() }.toSortedMap(),
                amountsMap = (if (amounts.isEmpty()) {
                    mutableMap.mapValues { it.value.size }
                        .let { mutableMapOf<PlaceWrapper, Int>().apply { putAll(it) } }
                } else amounts).toSortedMap()
            )
        }

        fun of(map: Map<PlaceWrapper, List<TokenWrapper>>): SimpleTokenSlice {
            return build {
                for ((place, tokens) in map) {
                    addTokens(place, tokens)
                }
            }

        }

        fun ofAmounts(map: Map<PlaceWrapper, Int>): SimpleTokenSlice {
            return build {
                for ((place, value) in map) {
                    addAmount(place, value)
                }
            }
        }
    }
}

class SimpleHistoricalTokenSlice(
    val simpleTokenSlice: SimpleTokenSlice,
    var buffHistoryEntry: Long? = null,
) :
    TokenSlice by simpleTokenSlice,
    HistoricalTokenSlice {
    override val historyEntry: Long
        get() = buffHistoryEntry!!

    override fun takeSnapshot(): String {
        return buildString {
            appendLine("historical token slice, entry: $historyEntry")
            appendLine(
                simpleTokenSlice.takeSnapshot().prependIndent()
            )
        }
    }

    fun clear() {
        simpleTokenSlice.clearTokens()
        buffHistoryEntry = null
    }

    fun copy(): SimpleHistoricalTokenSlice {
        return SimpleHistoricalTokenSlice(
            simpleTokenSlice = simpleTokenSlice.deepCopy() as SimpleTokenSlice,
            buffHistoryEntry = historyEntry
        )
    }
}

fun Collection<TokenSlice>.toStringSnapshot(): String {
    return joinToString("-------------\n") { it.takeSnapshot() }
}

interface ConstructionBlock {
    fun addRelatedPlace(relatedPLace: PlaceWrapper)
    fun addTokens(placeWrapper: PlaceWrapper, tokens: Collection<TokenWrapper>)
    fun addAmount(placeWrapper: PlaceWrapper, amount: Int)
}