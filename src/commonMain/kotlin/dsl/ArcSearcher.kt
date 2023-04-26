package dsl

class ArcSearcher(private val arcContainer: ArcContainer) {
    private val arcs : List<ArcDSL>
        get() = arcContainer.arcs
    fun inputArcFor(placeDSL: NodeDSL): ArcDSL {
        return arcs.find { it.arrowAtom == placeDSL }!!
    }

    fun allArcsFor(nodeDSL: NodeDSL): List<ArcDSL> {
        return arcs.filter { it.arrowAtom == nodeDSL || it.tailAtom == nodeDSL }
    }

    fun inputArcsFor(placeDSL: NodeDSL): List<ArcDSL> {
        return arcs.filter { it.arrowAtom == placeDSL }
    }

    fun outputArcFor(placeDSL: NodeDSL): ArcDSL {
        return arcs.find { it.tailAtom == placeDSL }!!
    }

    fun outputArcsFor(placeDSL: NodeDSL): List<ArcDSL> {
        return arcs.filter { it.tailAtom == placeDSL }
    }

}
