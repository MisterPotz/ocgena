package ru.misterpotz.input.converter

import model.PetriAtom

class TreeElementWrapper(
    val petriAtom: PetriAtom,
    var subgraphIndex: Int?
)
