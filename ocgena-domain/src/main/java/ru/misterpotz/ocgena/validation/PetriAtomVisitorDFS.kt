package ru.misterpotz.ocgena.validation

import ru.misterpotz.model.atoms.Arc
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.atoms.Transition

interface PetriAtomVisitorDFS {

    fun visitArc(arc: Arc)

    fun visitTransition(transition: Transition)

    fun visitPlace(place: Place)

    fun cleanStack()
}

