package converter

import model.Arc
import model.ConsistencyCheckError
import model.PetriNode
import model.Place
import model.Transition

interface OCNetElements {
    val places : List<Place>

    val transitions: List<Transition>

    val arcs : List<Arc>

    val allPetriNodes : List<PetriNode>
}

interface OCNetErrorService {
    // observed consistency errors for given petri id
    fun errorsForPetriAtomId(petriAtomId : String) : List<ConsistencyCheckError>
}

expect class OCGraphvizConverter {
    fun renderFile
}

interface OCDotDeclaration {
    // assumption: it is ordered list of statements
    fun getFilteredAttributes(nodeLabel: String): String

}

// probably it would be better to generate domain models at the same time
// with editing the original declaration
// but for now lets forget about the optimization lol
class OCGraphvizGenerator(
    private val originalOCDOtDeclaration : OCDotDeclaration,
    private val ocNetElements: OCNetElements,
    // to understand which nodes should be highlighted
    private val ocnetErrorService: OCNetErrorService
) {
    fun compileGraphviz() : String {
        val stringBuilder = StringBuilder()

        // must follow the structure of original declaration (because can contain subgraphs)
        originalOCDOtDeclaration
//        for (place in ocNetElements.places) {
//            """${place.label} [ ${originalOCDOtDeclaration.getFilteredAttributes(place.id)} ];
//            """
//        }
//        for (arc in ocNetElements.arcs) {
//            """${arc.id} [ ${originalOCDOtDeclaration.getFilteredAttributes(arc.id)}];
//            """
//        }

    }

    fun arc
}
