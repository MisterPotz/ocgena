package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenBatchList
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import java.util.*
import javax.inject.Inject

class TokenBatchListFactory @Inject constructor(
    private val tokenGroupingStrategy: TokenGroupedInfo.TokenGroupingStrategy
) {
    fun createFor(): TokenBatchList {
        return TokenBatchList(
            tokenGroupingStrategy = tokenGroupingStrategy
        )
    }
}

class TokenGroupedInfoImpl(
    override val transition: Transition,
    tokenBatchListFactory: TokenBatchListFactory,
    ocNet: OCNet,
    override val tokenGroupingStrategy: TokenGroupedInfo.TokenGroupingStrategy,
) : TokenGroupedInfo {
    private val tokenBatchList: TokenBatchList = tokenBatchListFactory.createFor()
    private val bufferizedPlaces = mutableSetOf<PetriAtomId>()
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    private val arcsRegistry = ocNet.arcsRegistry

    fun group(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>) {
        if (bufferizedPlaces.contains(place)) return
        bufferizedPlaces.add(place)

        val objectType = placeToObjectTypeRegistry[place]
        val arcId = place.arcIdTo(transition.id)
        val arcMeta = arcsRegistry[arcId].arcMeta

        tokenBatchList.addTokens(
            objectTypeId = objectType,
            arcMeta = arcMeta,
            sortedSet = tokens
        )
    }

    override fun getTokenSetBy(toPlaceObjectTypeId: ObjectTypeId, outputArcMeta: ArcMeta): TokenSet? {
        return tokenBatchList.getTokenSetBy(toPlaceObjectTypeId, outputArcMeta)
    }

//    override fun getInputArcs(): Collection<Arc> {
//        return arcPerBatchSize.keys
//    }
//
//    override fun getTokenAmountComingThroughInputArc(arc: Arc): Int {
//        return arcPerBatchSize[arc]!!
//    }
}
