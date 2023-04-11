package converter

import ast.*
import declarations.PeggySyntaxError
import dsl.OCNetDSLElementsImpl
import dsl.OCScopeImpl
import dsl.OCScopeImplCreator
import dsl.ObjectTypeImpl
import dsl.PlaceDSL
import kotlinx.js.jso
import model.ErrorLevel

class OCDotPlacesCollector(val ocScopeCreator: OCScopeImpl) {
    fun collect(rootParsedStructure: OcDot): List<PlaceDSL> {

    }
}

class ASTVisitorPath(val path: MutableList<ASTBaseNode>) {
    fun push(ast: ASTBaseNode) {
        path.add(ast)
    }

    fun pop(ast: ASTBaseNode) {
        if (path.isEmpty()) return
        path.removeLast()
    }

    fun isAtBlockOf(type: dynamic /* Types */): Boolean {
        return path.last().type == type
    }

    fun isAtOcNet(): Boolean {
        return isAtBlockOf(Types.Ocnet)
    }

    fun isAtSubgraph(): Boolean {
        return isAtBlockOf(Types.Subgraph)
    }
}

abstract class PathAcceptingASTVisitorBFS() : OCDotASTVisitorBFS, PathAcceptorVisitorBFS {
    protected var path: ASTVisitorPath? = null
    protected val currentPath: ASTVisitorPath
        get() = path!!

    val last: ASTBaseNode
        get() = currentPath.path.last()

    override fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS {
        path = astVisitorPath
        return this
    }
}

object Utils {
    fun filterElementsByType(elements: Array<ASTBaseNode>, type: dynamic): List<dynamic> {
        return elements.filter { it.type == type }
    }
}


data class SemanticError(
    val message: String,
    val relatedAst: ASTBaseNode,
    val level: ErrorLevel,
)

class DSLElementsContainer(val ocScopeImpl: OCScopeImpl) {
    private val places: MutableMap<String, Node> = mutableMapOf()

    private val objectTypes : MutableMap<String, Node> = mutableMapOf()

    private val transitions: MutableMap<String, Node> = mutableMapOf()

    private val edgeBlocks: MutableList<Edge> = mutableListOf()

    val savedPlaces : Map<String,Node>
        get() = places
    val savedTransitions : Map<String, Node>
        get() = transitions

    val savedEdgeBlocks : List<Edge>
        get() = edgeBlocks
    fun rememberPlace(place : Node) {
        places[place.id.value] = place
    }

    fun rememberObjectType(objectType : Node) {
        objectTypes[objectType.id.value] = objectType
    }

    fun rememberTransition(transition: Node) {
        transitions[transition.id.value] = transition
    }

    fun rememberEdgeBlock(edge: Edge) {
        edgeBlocks.add(edge)
    }
}

class SemanticErrorReporterContainer() : SemanticErrorReporter {
    private val collectedErrors = mutableListOf<SemanticError>()

    fun pushError(error: SemanticError) {
        collectedErrors.add(error)
    }

    override fun collectReport(): List<SemanticError> {
        return collectedErrors
    }
}

class StructureCheckASTVisitorBFS(
    private val errorReporterContainer: SemanticErrorReporterContainer,
) : PathAcceptingASTVisitorBFS(), SemanticErrorReporter by errorReporterContainer {

    fun pushError(error: SemanticError) {
        errorReporterContainer.pushError(error)
    }

    fun countElements(array: Array<ASTBaseNode>, type: dynamic): Int {
        return array.count { it.type == type }
    }

    override fun visitOCDot(ast: OcDot) {
        val ocNets = countElements(ast.body, Types.Ocnet)

        if (ocNets > 1) {
            pushError(
                SemanticError(
                    message = "Only 1 ocnet block is allowed",
                    relatedAst = ast,
                    level = ErrorLevel.CRITICAL
                )
            )
        }
        if (ocNets == 0) {
            pushError(
                SemanticError(
                    message = "At least 1 ocnet block must be defined",
                    relatedAst = ast,
                    level = ErrorLevel.WARNING
                )
            )
        }
    }

    override fun visitOCNet(ast: OcNet) {
        val placesBlockCount = ast.body.count {
            it.type == Types.Subgraph
                    && (it as Subgraph).specialType == SubgraphSpecialTypes.Places
        }

        val transitionBlockCount = ast.body.count {
            it.type == Types.Subgraph
                    && (it as Subgraph).specialType == SubgraphSpecialTypes.Transitions
        }

        if (placesBlockCount == 0 || transitionBlockCount == 0) {
            pushError(
                SemanticError(
                    message = "places or transitions blocks are missing",
                    relatedAst = ast,
                    level = ErrorLevel.WARNING
                )
            )
        }
    }

    override fun visitNode(ast: Node) {

    }

    override fun visitEdge(ast: Edge) {

    }

    override fun visitSubgraph(ast: Subgraph) {

    }
}

class EdgeASTVisitorBFS() : OCDotASTVisitorBFS, PathAcceptorVisitorBFS {
    override fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS {
        TODO("Not yet implemented")
    }

    override fun visitOCDot(ast: OcDot) {
        TODO("Not yet implemented")
    }

    override fun visitOCNet(ast: OcNet) {
        TODO("Not yet implemented")
    }

    override fun visitNode(ast: Node) {
        TODO("Not yet implemented")
    }

    override fun visitEdge(ast: Edge) {
        TODO("Not yet implemented")
    }

    override fun visitSubgraph(ast: Subgraph) {

    }
}

interface SemanticErrorReporter {
    fun collectReport(): List<SemanticError>
}

val Subgraph.isTransitionsBlock: Boolean
    get() = specialType == SubgraphSpecialTypes.Transitions

val Subgraph.isPlacesBlock: Boolean
    get() = specialType == SubgraphSpecialTypes.Places

class NormalSubgraphHelper(
    val dslElementsContainer: DSLElementsContainer,
    val errorReporterContainer: SemanticErrorReporterContainer) : SemanticErrorReporter by errorReporterContainer {

    fun checkElementCanBeSaved(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Edge
    }

    fun trySaveSubgraphEntities(ast: Subgraph) {
        for (entity in ast.body) {
            dslElementsContainer.savedPlaces
        }
    }
}

class ElementsDeclarationsASTVisitorBFS(
    val dslElementsContainer: DSLElementsContainer,
    private val semanticErrorReporter: SemanticErrorReporterContainer = SemanticErrorReporterContainer(),
) : PathAcceptingASTVisitorBFS(), SemanticErrorReporter by semanticErrorReporter {

    private val specialSubgraphHelper = SpecialSubgraphHelper(dslElementsContainer, semanticErrorReporter)

    override fun visitOCDot(ast: OcDot) {}

    override fun visitOCNet(ast: OcNet) {}

    override fun visitNode(ast: Node) {

    }

    override fun visitEdge(ast: Edge) {
        TODO("Not yet implemented")
    }

    override fun visitSubgraph(ast: Subgraph) {
        specialSubgraphHelper.trySaveSubgraphEntities(ast)
    }

    override fun collectReport(): List<SemanticError> {
        return semanticErrorReporter.collectReport()
    }
}

interface PathAcceptorVisitorBFS {
    fun withPath(astVisitorPath: ASTVisitorPath): OCDotASTVisitorBFS
}

interface OCDotASTVisitorBFS {
    fun visitOCDot(ast: OcDot)
    fun visitOCNet(ast: OcNet)
    fun visitNode(ast: Node)
    fun visitEdge(ast: Edge)
    fun visitSubgraph(ast: Subgraph)
}

class DelegateOCDotASTVisitorBFS(
    val visitors: List<PathAcceptorVisitorBFS>,
) : OCDotASTVisitorBFS {
    val currentPath = ASTVisitorPath(mutableListOf())

    override fun visitOCDot(ast: OcDot) {
        currentPath.push(ast)
        for (astNode in ast.body) {
            val castAstNode = astNode as ASTBaseNode
            when (castAstNode.type) {
                Types.Ocnet -> {
                    doDelegateVisit(castAstNode as OcNet)
                    visitOCNet(castAstNode)
                }

                Types.Comment -> {
                    // skip
                }
            }
        }
        currentPath.pop(ast)
    }

    private fun <T : ASTBaseNode> doDelegateVisit(astNode: T) {
        for (visitor in visitors) {
            val visitorForPath = visitor.withPath(currentPath)

            with(visitorForPath) {
                when (astNode.type) {
                    Types.Ocnet -> visitOCNet(astNode as OcNet)
                    Types.Attribute -> Unit
                    Types.Attributes -> Unit
                    Types.Edge -> visitEdge(astNode as Edge)
                    Types.Subgraph -> visitSubgraph(astNode as Subgraph)
                    Types.Node -> visitNode(astNode as Node)
                    else -> Unit
                }
            }
        }
    }

    override fun visitOCNet(ast: OcNet) {
        currentPath.push(ast)

        for (stmt in ast.body) {
            val castNode = stmt as ASTBaseNode
            doDelegateVisit(castNode)

        }

        currentPath.pop(ast)
    }

    override fun visitNode(ast: Node) = Unit

    override fun visitEdge(ast: Edge) = Unit

    override fun visitSubgraph(ast: Subgraph) = Unit
}

class OCDotTransitionsCollector() {

}

actual class OCDotParser {

    private val ocScopeImpl = OCScopeImplCreator().createRootOCScope()
    private val dslElementsContainer = DSLElementsContainer(ocScopeImpl)

    private val placesCollector = OCDotPlacesCollector(ocScopeImpl)
    private val transitionsCollector = OCDotTransitionsCollector()
    private val delegateOCDotASTVisitorBFS = DelegateOCDotASTVisitorBFS(
        listOf(

        )
    )


    actual fun parse(ocDot: String): OCDotParseResult {
        val rule = jso<ParseOption__0> {
            rule = Types.OcDot
        }
        // always start parce from the root
        val parsedStructure = kotlin.runCatching {
            ast.parse(ocDot, rule)
        }

        if (parsedStructure.isSuccess) {
            // start parsing
            val result = parsedStructure.getOrThrow()
            console.log(result)

            return OCDotParseResult.Success(
                ocNetDSLElements = OCNetDSLElementsImpl(
                    mutableMapOf(),
                    mapOf(),
                    mutableListOf(),
                    mapOf(),
                    ObjectTypeImpl(
                        id = 0,
                        label = "stub"
                    )
                ),
                ocDotDeclaration = DefaultOCNetDeclaration()
            )
        } else {
            // show error
            val error = parsedStructure.exceptionOrNull()
            console.log(error)
            val peggyError = error as PeggySyntaxError
            console.log(peggyError)
            val casted = error as PeggySyntaxError
            val location = casted.location


            location.source?.let {
                console.log(it)
            }
            val fileRange = JsToCommonMapping.fileRange(casted.location)

            val source = fileRange.source
            if (source == null) {
                console.log("source is null")
            }
            val bro = fileRange.start.offset
            val nig: Int = 4 + bro

            return OCDotParseResult.Error(location = fileRange)
        }
    }
}

object JsToCommonMapping {
    fun filePosition(filePosition: declarations.FilePosition): FilePosition {
        return object : FilePosition {
            override val offset: Int = filePosition.offset.toInt()
            override val line: Int = filePosition.line.toInt()
            override val column: Int = filePosition.column.toInt()
        }
    }

    fun fileRange(fileRange: declarations.FileRange): FileRange {
        return object : FileRange {
            override val start: FilePosition = filePosition(fileRange.start)
            override val end: FilePosition = filePosition(fileRange.end)
            override val source: String? = fileRange.source
        }
    }
}
