package ru.misterpotz.ocgena.validation

import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition

interface PetriAtomVisitorDFS {

    fun visitArc(arc: Arc)

    fun visitTransition(transition: Transition)

    fun visitPlace(place: Place)

    fun cleanStack()
}

