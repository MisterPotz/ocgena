package ru.misterpotz.ocgena.simulation.binding

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.typea.TokenBuffer
import java.util.*

interface LockedTokenMoveStrategy {
    fun createBufferizer(): LockedTokenBufferizer
}

interface LockedTokenBufferizer {
    fun bufferize(place: PetriAtomId, tokens: SortedSet<ObjectTokenId>)
    fun createProducer(): OutputTokensProducer
}

interface OutputTokensProducer {
    fun produceByConsumeTokenBuffer(place: PetriAtomId)
    fun produceByNewTokenGeneration(place: PetriAtomId)
    fun finalTokenBufferForPlace(place: PetriAtomId) : TokenBuffer
}