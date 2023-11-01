package ru.misterpotz.input.converter

import model.*
import ru.misterpotz.input.converter.ext.arcTailId
import ru.misterpotz.input.converter.ext.arcArrowId

interface OCDotCompressedAST {
    val placeTyping: PlaceTyping
    val transitions: Transitions
    val places: Places
}

interface SPetriAtom {
    val id: PetriAtomId
}

interface PetriAtomRegistry {
    operator fun get(petriAtomId: PetriAtomId): PetriAtom
}

interface ForTransitionPlace {

}
t
\ | /
- p -
/ | \
t

transition - arc - place

interface TailBlock {
    fun getArrowAtom(): PetriAtomId
}

interface ArrowBlock {
    fun getTailAtom(): PetriAtomId
}

class PetriAtomRegistryImpl(
    private val map: Map<PetriAtomId, PetriAtom> = mutableMapOf(),
    private val arcs : Arcs
) : PetriAtomRegistry {
    private val inputArcsIndex = mutableMapOf<PetriAtomId, PetriAtomId>()
    private val outputArcsIndex = mutableMapOf<PetriAtomId, PetriAtomId>()

    inner class TailBlock(val tailAtom : PetriAtomId) : ru.misterpotz.input.converter.TailBlock {
        override fun getArrowAtom(): PetriAtomId {
            return outputArcsIndex.getOrPut(tailAtom) {

            }
        }
    }

    inner class ArrowBlock : ru.misterpotz.input.converter.ArrowBlock {
        override fun getTailAtom(): PetriAtomId {
            TODO("Not yet implemented")
        }
    }

    fun forArcWhereTailIs(petriAtomId: PetriAtomId): TailBlock {
        return TailBlock(petriAtomId)
    }

    fun forArcWhereArrowIs(petriAtomId: PetriAtomId): ArrowBlock {

    }

    override fun get(petriAtomId: PetriAtomId): PetriAtom {
        return requireNotNull(map[petriAtomId]) {
            "petri atom wasn't found for id $petriAtomId"
        }
    }
}

data class SPlace(
    override val id: PetriAtomId,
    val label: String,
    val inputArcs: MutableList<PetriAtomId> = mutableListOf(),
    val outputArcs: MutableList<PetriAtomId> = mutableListOf(),
) : SPetriAtom

data class SArc(
    override val id: PetriAtomId,
) : SPetriAtom {
    val arrowNodeId by lazy(mode = LazyThreadSafetyMode.NONE) {
        id.arcArrowId()
    }
    val tailNodeId by lazy(mode = LazyThreadSafetyMode.NONE) {
        id.arcTailId()
    }
}
