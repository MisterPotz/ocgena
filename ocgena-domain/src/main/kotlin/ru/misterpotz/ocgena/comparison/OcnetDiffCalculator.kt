package ru.misterpotz.ocgena.comparison

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

data class OCNetStructDiff(
    val original : OCNetStruct,
    val removedNodes : List<PetriAtomId>,
    val derivative: OCNetStruct,
    val newNodes : List<PetriAtomId>
)

class OcnetDiffCalculator(
    val original : OCNetStruct,
    val derivative : OCNetStruct,
) {


    fun calculateDiff() {
        val rawRemovedNodes = mutableSetOf<PetriAtomId>()
        val rawNewNodes = mutableSetOf<PetriAtomId>()
        val derivativeNodes = derivative.petriAtomRegistry.map.keys.toSet()
        val originalNodes = original.petriAtomRegistry.map.keys.toSet()

        val totalNewNodes = derivativeNodes.minus(originalNodes)
        val totalRemovedNodes = originalNodes.minus(derivativeNodes)

        println()
    }
}