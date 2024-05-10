package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.OCNetAccessor
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.Places
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.TokenWrapper
import ru.misterpotz.ocgena.utils.PatternIdCreator
import java.util.SortedSet

//class SimulationV2State {
//    val transitions: Transitions
//    val places : Places
//
//    companion object {
//        fun create(ocNetStruct: OCNetStruct): SimulationV2State {
//
//        }
//    }
//}

interface TokenSlice {
    val relatedPlaces: Set<PlaceWrapper>

    fun withPlaceFilter(places: Places): TokenSlice
    fun setAmount(placeWrapper: PlaceWrapper, new: Int)
    fun modifyTokensAt(placeWrapper: PlaceWrapper, modifyBlock: (MutableSortedTokens) -> Unit)
    fun amountAt(placeWrapper: PlaceWrapper): Int
    fun tokensAt(placeWrapper: PlaceWrapper): SortedTokens
    fun minusInPlace(tokenSlice: TokenSlice)
    fun plusInPlace(tokenSlice: TokenSlice)
}

class TokenStore(
    val issuedTokens: MutableMap<String, TokenWrapper> = mutableMapOf(),
    private val internalSlice: TokenSlice,
    private val ocNetAccessor: OCNetAccessor,
) : TokenSlice by internalSlice {
    private val tokenCreators = TokenCreators()

    fun generateRealToken(typeId: ObjectTypeId): TokenWrapper {
        val type = ocNetAccessor.ocNet.objectTypeRegistry[typeId]

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

    override fun withPlaceFilter(places: Places): TokenSlice {
        return copy(internalRelatedPlaces = internalRelatedPlaces.intersect(places).toMutableSet())
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
}