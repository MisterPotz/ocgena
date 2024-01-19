package ru.misterpotz.ocgena.simulation.binding.buffer

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.TokenBatchList
import ru.misterpotz.ocgena.simulation.binding.TokenGroup
import java.util.*
import javax.inject.Inject

class TokenBatchListFactory @Inject constructor(
    private val tokenGroupingStrategy: TransitionGroupedTokenInfo.TokenGroupingStrategy
) {
    fun createFor(): TokenBatchList {
        return TokenBatchList(
            tokenGroupingStrategy = tokenGroupingStrategy
        )
    }
}

class TransitionGroupedTokenInfoImpl(
    override val transition: Transition,
    private val petriAtomRegistry: PetriAtomRegistry,
    tokenBatchListFactory: TokenBatchListFactory,
    ocNet: OCNet,
    override val tokenGroupingStrategy: TransitionGroupedTokenInfo.TokenGroupingStrategy,
) : TransitionGroupedTokenInfo {
    private val tokenBatchList: TokenBatchList = tokenBatchListFactory.createFor()
    private val arcPerBatchSize = mutableMapOf<Arc, Int>()
    private val bufferizedPlaces = mutableSetOf<PetriAtomId>()
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    private val arcsRegistry = ocNet.arcsRegistry

    fun bufferize(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>) {
        if (bufferizedPlaces.contains(place)) return
        bufferizedPlaces.add(place)

        val arc = petriAtomRegistry.getArc(place.arcIdTo(transition.id))
        val batchSize = tokens.size
        arcPerBatchSize[arc] = batchSize

        val objectType = placeToObjectTypeRegistry[place]
        val arcId = place.arcIdTo(transition.id)
        val arcMeta = arcsRegistry[arcId].arcMeta

        tokenBatchList.addTokens(
            objectTypeId = objectType,
            arcMeta = arcMeta,
            sortedSet = tokens
        )
    }

    override fun getGroup(toPlaceObjectTypeId: ObjectTypeId, outputArcMeta: ArcMeta): TokenGroup? {
        return tokenBatchList.getBatchBy(toPlaceObjectTypeId, outputArcMeta)
    }

//    override fun getInputArcs(): Collection<Arc> {
//        return arcPerBatchSize.keys
//    }
//
//    override fun getTokenAmountComingThroughInputArc(arc: Arc): Int {
//        return arcPerBatchSize[arc]!!
//    }
}
