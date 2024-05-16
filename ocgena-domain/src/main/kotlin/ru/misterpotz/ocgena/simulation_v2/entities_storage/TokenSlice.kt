package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.utils.PatternIdCreator
import ru.misterpotz.ocgena.utils.buildMutableMap
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
    fun modifyTokensAt(placeWrapper: PlaceWrapper, modifyBlock: (MutableSortedTokens) -> Unit)
    fun amountAt(placeWrapper: PlaceWrapper): Int
    fun tokensAt(placeWrapper: PlaceWrapper): SortedTokens
    fun minusInPlace(tokenSlice: TokenSlice)
    fun plusInPlace(tokenSlice: TokenSlice)
    fun filterTokensInPlaces(places: Places, predicate: (TokenWrapper, PlaceWrapper) -> Boolean): TokenSlice
    fun byPlaceIterator(): Iterator<Pair<PlaceWrapper, SortedTokens>>
    fun print()
}

interface TokenGenerator {
    fun generateRealToken(type: ObjectType): TokenWrapper
}

class TokenStore(
    private val internalSlice: SimpleTokenSlice,
    private val modelAccessor: ModelAccessor,
    private val issuedTokens: MutableMap<Long, TokenWrapper> = mutableMapOf(),
) : TokenSlice by internalSlice, TokenGenerator {
    private val tokenCreators = TokenCreators()

    fun removeToken(tokenWrapper: TokenWrapper) {
        issuedTokens.remove(tokenWrapper.tokenId)
    }

    fun removeTokens(tokens: Collection<TokenWrapper>) {
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
}

class TokenCreators() {
    private val creators = mutableMapOf<ObjectType, SingleTypeTokenCreator>()
    fun create(objectType: ObjectType): TokenWrapper {
        return creators.getOrPut(objectType) {
            SingleTypeTokenCreator(type = objectType)
        }.makeNew()
    }
}

class SingleTypeTokenCreator(
    val type: ObjectType,
    private val idIssuer: PatternIdCreator = PatternIdCreator() {
        "${type.id}$it"
    }
) {
    fun makeNew(): TokenWrapper {
        return TokenWrapper(
            tokenId = idIssuer.newIntId,
            objectType = type,
        )
    }
}

typealias MutableSortedTokens = SortedSet<TokenWrapper>
typealias SortedTokens = Set<TokenWrapper>

class SimpleTokenSlice(
    private val internalRelatedPlaces: SortedSet<PlaceWrapper>,
    private val tokensMap: SortedMap<PlaceWrapper, MutableSortedTokens> = sortedMapOf(),
    private val amountsMap: SortedMap<PlaceWrapper, Int> = sortedMapOf()
) : TokenSlice {
    init {
        for (i in internalRelatedPlaces) {
            tokensMap.getOrPut(i) { sortedSetOf() }
            amountsMap.getOrPut(i) { 0 }
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

    override fun print() {
        buildString {
            appendLine("tokensmap:")

            appendLine(
                buildString {
                    for ((place, tokens) in tokensMap) {
                        appendLine(
                            "${place.placeId} (size:${tokens.size}) ->  ${
                                tokens.joinToString(
                                    ",",
                                    prefix = "[",
                                    postfix = "]"
                                )
                            }"
                        )
                    }
                }.prependIndent()
            )
        }.let {
            println(it)
        }
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

    override fun filterTokensInPlaces(
        places: Places,
        predicate: (TokenWrapper, PlaceWrapper) -> Boolean
    ): SimpleTokenSlice {
        val filteredRelatedPlaces = internalRelatedPlaces.intersect(places)

        val tokensMap: MutableMap<PlaceWrapper, MutableSortedTokens> = buildMutableMap {
            for (place in filteredRelatedPlaces) {
                val tokens = tokensAt(place).toMutableSet()
                put(place, sortedSetOf<TokenWrapper>().apply {
                    addAll(tokens.filter { token -> predicate(token, place) })
                })
            }
        }
        val newAmountMap: MutableMap<PlaceWrapper, Int> = buildMutableMap {
            for (place in filteredRelatedPlaces) {
                val original = amountAt(place)
                val originalTokenSize = tokensAt(place).size
                val newTokenSize = tokensMap[place]!!.size
                val tokenDiff = originalTokenSize - newTokenSize
                require(tokenDiff >= 0)
                val newamount = original - tokenDiff
                require(newamount >= 0)
                put(place, newamount)
            }
        }

        return SimpleTokenSlice(
            filteredRelatedPlaces.toSortedSet(),
            tokensMap.toSortedMap(),
            newAmountMap.toSortedMap()
        )
    }

    override fun setAmount(placeWrapper: PlaceWrapper, new: Int) {
        if (placeWrapper !in relatedPlaces) return
        amountsMap[placeWrapper] = new
    }

    private fun addPlaceIfNeed(placeWrapper: PlaceWrapper) {
        tokensMap.getOrPut(placeWrapper) {
            sortedSetOf<TokenWrapper>()
        }
        amountsMap.getOrPut(placeWrapper) {
            0
        }
        internalRelatedPlaces.add(placeWrapper)
    }

    override fun modifyTokensAt(placeWrapper: PlaceWrapper, modifyBlock: (MutableSortedTokens) -> Unit) {
        if (placeWrapper !in relatedPlaces) return
        tokensMap[placeWrapper]!!.let(modifyBlock)
    }

    override fun amountAt(placeWrapper: PlaceWrapper): Int {
        if (placeWrapper !in relatedPlaces) return 0

        return amountsMap[placeWrapper]!!
    }

    override fun tokensAt(placeWrapper: PlaceWrapper): SortedTokens {
        if (placeWrapper !in relatedPlaces) return sortedSetOf()

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

            modifyTokensAt(place) {
                it.addAll(tokenSlice.tokensAt(place))
            }
            setAmount(place, amountAt(place) + tokenSlice.amountAt(place))
        }
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
            val mutableMap = mutableMapOf<PlaceWrapper, MutableSortedTokens>()

            val amounts = mutableMapOf<PlaceWrapper, Int>()

            val constructionBlock = object : ConstructionBlock {
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
                internalRelatedPlaces = mutableMap.keys.toMutableSet().union(amounts.keys).toSortedSet(),
                tokensMap = mutableMap.toSortedMap(),
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

interface ConstructionBlock {
    fun addTokens(placeWrapper: PlaceWrapper, tokens: Collection<TokenWrapper>)
    fun addAmount(placeWrapper: PlaceWrapper, amount: Int)
}