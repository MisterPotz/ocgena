package simulation.binding

import model.Arcs
import model.OcNetType
import model.PlaceTyping
import simulation.ObjectTokenGenerator
import simulation.ObjectTokenMoverFactory
import simulation.typea.BindingOutputMarkingTypeAResolver

class BindingOutputMarkingResolverFactory(
    private val arcs: Arcs,
    private val ocNetType: OcNetType,
    private val placeTyping: PlaceTyping,
    private val objectTokenGenerator: ObjectTokenGenerator,
    private val objectTokenMoverFactory: ObjectTokenMoverFactory
) {
    fun create() : InputToOutputPlaceResolver {
        return when (ocNetType) {
            OcNetType.TYPE_A -> BindingOutputMarkingTypeAResolver(
                arcs = arcs,
                placeTyping = placeTyping,
                objectTokenGenerator = objectTokenGenerator,
                objectTokenMoverFactory = objectTokenMoverFactory
            )
            OcNetType.TYPE_L -> TODO("I.A.Lomazova specification is yet to be done")
        }
    }
}
