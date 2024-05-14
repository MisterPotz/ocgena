package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Places
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.utils.PatternIdCreator
import ru.misterpotz.ocgena.utils.buildMutableMap
import java.util.SortedSet

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

class TokenStore(
    private val internalSlice: SimpleTokenSlice,
    private val modelAccessor: ModelAccessor,
    val issuedTokens: MutableMap<String, TokenWrapper> = mutableMapOf(),
) : TokenSlice by internalSlice {
    private val tokenCreators = TokenCreators()

    fun generateRealToken(typeId: ObjectTypeId): TokenWrapper {
        val type = modelAccessor.ocNet.objectTypeRegistry[typeId]

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
            tokenId = idIssuer.newIdGetLabel(),
            objectType = type,
        )
    }
}

typealias MutableSortedTokens = SortedSet<TokenWrapper>
typealias SortedTokens = Set<TokenWrapper>

data class SimpleTokenSlice(
    private val internalRelatedPlaces: MutableSet<PlaceWrapper>,
    private val tokensMap: MutableMap<PlaceWrapper, MutableSortedTokens> = mutableMapOf(),
    private val amountsMap: MutableMap<PlaceWrapper, Int> = mutableMapOf()
) : TokenSlice {
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

    fun tokenBy(id: String): TokenWrapper {
        return tokensMap.values.find { it.find { it.tokenId == id } != null }!!.find { it.tokenId == id }!!
    }

    override fun withPlaceFilter(places: Places): TokenSlice {
        return copy(internalRelatedPlaces = internalRelatedPlaces.intersect(places).toMutableSet())
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

        return SimpleTokenSlice(filteredRelatedPlaces.toMutableSet(), tokensMap, newAmountMap)
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
            require(totalPlace < 0) {
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

    companion object {
        fun build(block: ConstructionBlock.() -> Unit): SimpleTokenSlice {
            val mutableMap = mutableMapOf<PlaceWrapper, MutableSortedTokens>()

            val constructionBlock = object : ConstructionBlock {
                override fun addTokens(placeWrapper: PlaceWrapper, tokens: Collection<TokenWrapper>) {
                    mutableMap.getOrPut(placeWrapper) {
                        sortedSetOf()
                    }.addAll(tokens)
                }
            }
            constructionBlock.block()
            return SimpleTokenSlice(
                internalRelatedPlaces = mutableMap.keys.toMutableSet(),
                tokensMap = mutableMap,
                amountsMap = mutableMap.mapValues { it.value.size }
                    .let { mutableMapOf<PlaceWrapper, Int>().apply { putAll(it) } }
            )
        }

        fun of(map: Map<PlaceWrapper, List<TokenWrapper>>): SimpleTokenSlice {
            return build {
                for ((place, tokens) in map) {
                    addTokens(place, tokens)
                }
            }

        }
    }
}

interface ConstructionBlock {
    fun addTokens(placeWrapper: PlaceWrapper, tokens: Collection<TokenWrapper>)
}