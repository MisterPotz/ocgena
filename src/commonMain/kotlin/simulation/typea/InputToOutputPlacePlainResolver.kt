package simulation.typea

import model.Arcs
import model.Place
import model.Transition
import model.aalst.ArcMultiplicityTypeA
import simulation.binding.InputToOutputPlaceResolver

class InputToOutputPlacePlainResolver(
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs : Arcs,
) : InputToOutputPlaceResolver {
    override fun getOutputPlaceForInput(transition: Transition, inputPlace: Place): Place {
        val type = inputPlace.type
        val arc = arcs[transition][inputPlace]!!

        val outputPlaces = transition.outputPlaces.filter { it.type == type }
        val thePlace = outputPlaces.find { outputPlace ->
            val outputArc = arcs[transition][outputPlace]!!
            arcMultiplicity.multiplicitiesEqual(arc, outputArc)
        }
        requireNotNull(thePlace) {
            "output place for the given input arc and place type ($type) must present in the structure"
        }
        return thePlace
    }
}
