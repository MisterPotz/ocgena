package ru.misterpotz.ocgena.ocnet.primitives.ext

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

fun PetriAtomId.arcTailId(): PetriAtomId {
    val delimiterIndex = indexOf("_")
    require(delimiterIndex > 0) {
        "id was not of arc $this"
    }
    return substring(0, delimiterIndex)
}

fun PetriAtomId.arcArrowId(): PetriAtomId {
    val delimiterIndex = indexOf("_")
    require(delimiterIndex > 0 && length > delimiterIndex + 1) {
        "id was not of arc $this"
    }
    return substring(delimiterIndex + 1, length)
}

fun PetriAtomId.arcIdTo(petriAtomId: PetriAtomId): PetriAtomId {
    return "${this}_$petriAtomId"
}
