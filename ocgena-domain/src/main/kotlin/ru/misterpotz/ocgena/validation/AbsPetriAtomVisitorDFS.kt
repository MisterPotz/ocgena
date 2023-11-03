package ru.misterpotz.ocgena.validation

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtom
import ru.misterpotz.ocgena.ocnet.primitives.utils.RecursionProtector
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Arc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.PetriAtomRegistry

abstract class AbsPetriAtomVisitorDFS(
    private val petriAtomRegistry: PetriAtomRegistry
) : PetriAtomVisitorDFS {
    protected val recursionProtector = RecursionProtector()

    final override fun visitArc(arc: Arc) {
        recursionProtector.protectWithRecursionStack(arc.id) {
            val canStopParsing = doForArcBeforeDFS(arc)
            if (!canStopParsing) {
                petriAtomRegistry[arc.arrowNodeId!!].acceptVisitor(this)
            }
            doForAtomAfterDFS(arc)
        }
    }

    final override fun visitTransition(transition: Transition) {
        recursionProtector.protectWithRecursionStack(transition.id) {
            val canStopParsing = doForTransitionBeforeDFS(transition)
            if (!canStopParsing) {
                for (outputPlace in transition.outputPlaces) {
                    val arc = with(petriAtomRegistry) {
                        transition.id.arcTo(outputPlace)
                    }
                    visitArc(arc)
                }
            }
            doForAtomAfterDFS(transition)
        }
    }

    final override fun visitPlace(place: Place) {
        recursionProtector.protectWithRecursionStack(place.id) {
            val canStopParsing = doForPlaceBeforeDFS(place)
            if (!canStopParsing) {
                for (toTransition in place.toTransitions) {
                    val arc = with(petriAtomRegistry) {
                        place.id.arcTo(toTransition)
                    }
                    visitArc(arc)
                }
            }
            doForAtomAfterDFS(place)
        }
    }

    open fun doForAtomAfterDFS(atom: PetriAtom) = Unit
    open fun doForArcBeforeDFS(arc: Arc) : Boolean = false
    open fun doForTransitionBeforeDFS(transition: Transition) : Boolean = false
    open fun doForPlaceBeforeDFS(place: Place) : Boolean = false

    override fun cleanStack() {
        recursionProtector.clean()
    }
}