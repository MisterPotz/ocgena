package converter

import ast.*
import declarations.PeggySyntaxError
import dsl.OCScopeImpl
import dsl.OCScopeImplCreator
import kotlinx.js.Object
import kotlinx.js.jso
import model.ErrorLevel
import kotlin.reflect.KClass


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


data class SemanticErrorAST(
    val message: String,
    val relatedAst: ASTBaseNode,
    val level: ErrorLevel,
)

class SubgraphAssociations() {
    val namedSubgraphs: MutableMap<String, Subgraph> = mutableMapOf()

    fun rememberSubgraph(subgraph: Subgraph) {
        subgraph.id?.value?.let {
            namedSubgraphs[it] = subgraph
        }
    }

    fun containsSubgraph(subgraph: Subgraph): Boolean {
        return subgraph.id?.value in namedSubgraphs
    }
}

class DSLElementsContainer(val ocScopeImpl: OCScopeImpl) {
    private val places: MutableMap<String, Node> = mutableMapOf()

    private val objectTypes: MutableMap<String, Node> = mutableMapOf()

    private val transitions: MutableMap<String, Node> = mutableMapOf()

    private val edgeBlocks: MutableList<Edge> = mutableListOf()

    val subgraphAssociations: SubgraphAssociations = SubgraphAssociations()

    val placeToInitialMarking : MutableMap<String, Int> = mutableMapOf()
    val placeToObjectType : MutableMap<String, String> = mutableMapOf()
    val inputPlaceLabels : MutableList<String> = mutableListOf()
    val outputPlaceLabels : MutableList<String> = mutableListOf()

    val savedObjectTypes : Map<String, Node>
        get() = objectTypes
    val savedPlaces: Map<String, Node>
        get() = places
    val savedTransitions: Map<String, Node>
        get() = transitions

    val savedEdgeBlocks: List<Edge>
        get() = edgeBlocks

    fun rememberPlace(place: Node) {
        places[place.id.value] = place
    }

    fun rememberInitialMarkingForPlace(placeLabel : String, initialTokens : Int) {
        console.log("saving $placeLabel initial tokens $initialTokens")
        placeToInitialMarking[placeLabel] = initialTokens
    }

    fun rememberPlaceIsInput(placeLabel: String) {
        console.log("remembered input $placeLabel")
        inputPlaceLabels.add(placeLabel)
    }

    fun recallIfPlaceIsInput(placeLabel: String) : Boolean {
        val input = inputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is input $input")
        return input
    }

    fun recallIfPlaceIsOutput(placeLabel: String) : Boolean {
        val output = outputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is output $output")
        return output
    }

    fun rememberPlaceIsOutput(placeLabel: String) {
        console.log("remembered output $placeLabel")
        outputPlaceLabels.add(placeLabel)
    }

    fun rememberObjectTypeForPlace(placeLabel : String, objectTypeLabel : String) {
        placeToObjectType[placeLabel] = objectTypeLabel
    }

    fun rememberSubgraph(subgraph: Subgraph) {
        subgraphAssociations.rememberSubgraph(subgraph)
    }

    fun rememberObjectType(objectType: Node) {
        objectTypes[objectType.id.value] = objectType
    }

    fun recallObjectTypeForPlace(placeLabel: String) : String? {
        return placeToObjectType[placeLabel]
    }

    fun recallInitialTokensForPlace(placeLabel: String) : Int? {
        val value = placeToInitialMarking[placeLabel]
        console.log("for place $placeLabel have initial $value")
        return value
    }

    fun rememberTransition(transition: Node) {
        transitions[transition.id.value] = transition
    }

    fun rememberEdgeBlock(edge: Edge) {
        edgeBlocks.add(edge)
    }
}

class SemanticErrorReporterContainer() : SemanticErrorReporter {
    private val collectedErrors = mutableListOf<SemanticErrorAST>()

    fun pushError(error: SemanticErrorAST) {
        collectedErrors.add(error)
    }

    override fun collectReport(): List<SemanticErrorAST> {
        return collectedErrors
    }
}

class StructureCheckASTVisitorBFS(
    private val errorReporterContainer: SemanticErrorReporterContainer,
) : PathAcceptingASTVisitorBFS(), SemanticErrorReporter by errorReporterContainer {

    fun pushError(error: SemanticErrorAST) {
        errorReporterContainer.pushError(error)
    }

    fun countElements(array: Array<ASTBaseNode>, type: dynamic): Int {
        return array.count { it.type == type }
    }

    override fun visitOCDot(ast: OcDot) {
        val ocNets = countElements(ast.body, Types.Ocnet)

        if (ocNets > 1) {
            pushError(
                SemanticErrorAST(
                    message = "Only 1 ocnet block is allowed",
                    relatedAst = ast,
                    level = ErrorLevel.CRITICAL
                )
            )
        }
        if (ocNets == 0) {
            pushError(
                SemanticErrorAST(
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
                SemanticErrorAST(
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

interface SemanticErrorReporter {
    fun collectReport(): List<SemanticErrorAST>
}

val Subgraph.isTransitionsBlock: Boolean
    get() = specialType == SubgraphSpecialTypes.Transitions

val Subgraph.isPlacesBlock: Boolean
    get() = specialType == SubgraphSpecialTypes.Places

val Subgraph.isSpecial: Boolean
    get() = specialType != null

class NormalSubgraphHelper(
    val dslElementsContainer: DSLElementsContainer,
    val errorReporterContainer: SemanticErrorReporterContainer,
) : SemanticErrorReporter by errorReporterContainer {

    fun checkElementCanBeSaved(ast: ASTBaseNode): Boolean {
        return ast.type == Types.Node || ast.type == Types.Edge
    }

    fun checkNodeIsAcceptable(ast: ASTBaseNode): Boolean {
        return when (ast.type) {
            Types.Attribute, Types.Attributes, Types.Edge, Types.Node, Types.Subgraph, Types.Comment -> {
                true
            }

            else -> {
                false
            }
        }
    }

    fun trySaveSubgraph(ast: Subgraph) {
        if (ast.isSpecial) return
        dslElementsContainer.rememberSubgraph(ast)
    }
}

class ElementsDeclarationsASTVisitorBFS(
    val dslElementsContainer: DSLElementsContainer,
    private val semanticErrorReporter: SemanticErrorReporterContainer = SemanticErrorReporterContainer(),
) : PathAcceptingASTVisitorBFS(), SemanticErrorReporter by semanticErrorReporter {
    private val normalSubgraphHelper = NormalSubgraphHelper(dslElementsContainer, semanticErrorReporter)
    private val specialSubgraphHelper = SpecialSubgraphHelper(dslElementsContainer, semanticErrorReporter)

    override fun visitOCDot(ast: OcDot) {}

    override fun visitOCNet(ast: OcNet) {}

    override fun visitNode(ast: Node) {

    }

    override fun visitEdge(ast: Edge) {
        dslElementsContainer.rememberEdgeBlock(ast)
    }

    override fun visitSubgraph(ast: Subgraph) {
        specialSubgraphHelper.trySaveSubgraphEntities(ast)
        normalSubgraphHelper.trySaveSubgraph(ast)
    }

    override fun collectReport(): List<SemanticErrorAST> {
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

class SemanticParseToDSLConverter(dslElementsContainer: DSLElementsContainer) {

}


abstract class ChainStage<In : Any, Out : Any>(
    val inputClass: KClass<In>,
    val outputClass: KClass<Out>,
) {
    @Suppress("UNCHECKED_CAST")
    fun doPerform(input: Any): ChainResult<Out> {
        return perform(input as In)
    }

    protected abstract fun perform(input: In): ChainResult<Out>


    companion object {
        const val LOGGING = true
    }
}

data class ChainResult<T>(
    val success: T? = null,
    val failure: OCDotParseResult? = null,
) {
    val isSuccess
        get() = success != null

    val isFailure
        get() = failure != null
}

class SyntaxParsingStage() : ChainStage<String, Object>(String::class, outputClass = Object::class) {
    private fun tryParse(ocDot: String): Result<Object> {
        val rule = jso<ParseOption__0> {
            rule = Types.OcDot
        }
        // always start parce from the root
        return kotlin.runCatching {
            parse(ocDot, rule)
        }
    }

    private fun buildPeggyErrorMessage(peggySyntaxError: PeggySyntaxError): String {
        return PeggySyntaxError.buildMessage(peggySyntaxError.expected, peggySyntaxError.found)
    }

    private fun processParseFailure(exception: Throwable): OCDotParseResult {
        // show error
        val error = exception

        console.log(error)
        val peggyError = error as PeggySyntaxError
        console.log(peggyError)
        val location = peggyError.location


        location.source?.let {
            console.log(it)
        }

        val fileRange = JsToCommonMapping.fileRange(peggyError.location)
        val errorMessage = buildPeggyErrorMessage(peggyError)

        return OCDotParseResult.SyntaxParseError(message = errorMessage, location = fileRange)
    }

    override fun perform(input: String): ChainResult<Object> {
        val parseResult = tryParse(ocDot = input)

        return if (parseResult.isSuccess) {
            ChainResult(success = parseResult.getOrThrow())
        } else {
            ChainResult(failure = processParseFailure(parseResult.exceptionOrNull()!!))
        }
    }
}

class SemanticParsingStage(
    private val dslElementsContainer: DSLElementsContainer,
    private val delegateOCDotASTVisitorBFS: DelegateOCDotASTVisitorBFS,
) : ChainStage<Object, DSLElementsContainer>(Object::class, DSLElementsContainer::class) {
    private fun doSemanticASTParse(parsedStructure: Object): Result<Unit> {
        return kotlin.runCatching {
            delegateOCDotASTVisitorBFS.visitOCDot(parsedStructure as OcDot)
        }
    }

    override fun perform(input: Object): ChainResult<DSLElementsContainer> {
        val parseResult = doSemanticASTParse(input)

        return if (parseResult.isSuccess) {
            ChainResult(success = dslElementsContainer)
        } else {
            ChainResult(
                failure = OCDotParseResult.SemanticParseException(
                    message = "Unknown exception during semantic parse",
                    originalException = parseResult.exceptionOrNull()
                )
            )
        }
    }
}

class SemanticAnalysisPostProcessingStage(
    private val dslElementsContainer: DSLElementsContainer,
    private val errorReporterContainer: SemanticErrorReporterContainer,
) :
    ChainStage<DSLElementsContainer, DSLElementsContainer>(DSLElementsContainer::class, DSLElementsContainer::class) {
    private fun hasCriticalErrors(errors: List<SemanticErrorAST>): Boolean {
        return errors.find { it.level == ErrorLevel.CRITICAL } != null
    }

    private fun processSemanticAnalysisResult(): ChainResult<DSLElementsContainer> {
        val errors = errorReporterContainer.collectReport()


        return if (hasCriticalErrors(errors)) {
            ChainResult(
                failure = OCDotParseResult.SemanticCriticalErrorsFound(
                    message = "Encountered crititcal semantic errors during analysis",
                    collectedSemanticErrors = errors.map { it ->
                        SemanticError(
                            message = it.message,
                            relatedAst = ASTTypeLocation(
                                type = it.relatedAst.type,
                                location = JsToCommonMapping.fileRange(it.relatedAst.location)
                            ),
                            level = it.level
                        )
                    }
                )
            )

        } else {
            ChainResult(
                success = dslElementsContainer
            )
        }
    }

    override fun perform(input: DSLElementsContainer): ChainResult<DSLElementsContainer> {
        return processSemanticAnalysisResult()
    }
}

class DomainConversionStage(
    private val dslElementsContainer: DSLElementsContainer,
    private val errorReporterContainer: SemanticErrorReporterContainer,
) : ChainStage<DSLElementsContainer, OCDotParseResult.Success>(
    DSLElementsContainer::class,
    OCDotParseResult.Success::class
) {
    override fun perform(input: DSLElementsContainer): ChainResult<OCDotParseResult.Success> {
        val ocDotToDomainConverter = OCDotToDomainConverter()

        val result = ocDotToDomainConverter.convert(input)
        console.log(result)

        if (result.hasCriticalErrors) {
            return ChainResult(failure = OCDotParseResult.DomainCheckCriticalErrorsFound(
                message = "Domain checks failed for the given net",
                collectedSemanticErrors = result.errors
            ))
        } else {
            return ChainResult(
                success = OCDotParseResult.Success
            )
        }
    }
}

class ParsingChain(
    private val chainStages: List<ChainStage<*, *>>,
) {

    private fun checkOutputAndInputTypeConsistency(currentResult: KClass<*>, expectedInput: KClass<*>) {
        if (expectedInput::class.simpleName != currentResult::class.simpleName) {
            throw IllegalStateException("input class ${currentResult::class.simpleName} doesn't satisfy parsing stage required class ${expectedInput::class.simpleName}")
        }
    }

    fun process(input: Any): OCDotParseResult {
        var currentResult: Any = input

        for (chainStage in chainStages) {
            checkOutputAndInputTypeConsistency(currentResult::class, chainStage.inputClass)

            if (LOGGING) {
                console.log("start stage: pass input $currentResult -> to stage ${chainStage::class.simpleName} ")
            }
            val stageResult = chainStage.doPerform(currentResult)

            if (LOGGING) {
                console.log("finish stage: stage ${chainStage::class.simpleName} produced -> result: $stageResult")
            }
            if (stageResult.isSuccess) {
                currentResult = stageResult.success!!
            } else {
                return stageResult.failure!!
            }
        }

        return currentResult as OCDotParseResult
    }

    companion object {
        const val LOGGING = true
    }
}

actual class OCDotParser {

    private val ocScopeImpl = OCScopeImplCreator().createRootOCScope()
    private val dslElementsContainer = DSLElementsContainer(ocScopeImpl)
    private val errorReporterContainer = SemanticErrorReporterContainer()

    private val transitionsCollector = OCDotTransitionsCollector()
    private val structureCheckerVisitor = StructureCheckASTVisitorBFS(errorReporterContainer)
    private val elementsDeclarationsVisitor =
        ElementsDeclarationsASTVisitorBFS(dslElementsContainer, errorReporterContainer)

    private val delegateOCDotASTVisitorBFS = DelegateOCDotASTVisitorBFS(
        listOf(
            structureCheckerVisitor,
            elementsDeclarationsVisitor,
        )
    )

    actual fun parse(ocDot: String): OCDotParseResult {
        val parsingChain = ParsingChain(
            chainStages = listOf(
                SyntaxParsingStage(),
                SemanticParsingStage(dslElementsContainer, delegateOCDotASTVisitorBFS),
                SemanticAnalysisPostProcessingStage(dslElementsContainer, errorReporterContainer),
                DomainConversionStage(dslElementsContainer, errorReporterContainer)
            )
        )
        val result = parsingChain.process(ocDot)

        return result
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
