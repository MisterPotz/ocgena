package converter

import ast.*
import dsl.OCScopeImpl
import dsl.OCScopeImplCreator
import error.Error
import error.ErrorLevel
import kotlinx.js.Object
import kotlinx.js.jso
import parse.SemanticError
import kotlin.reflect.KClass


object Utils {
    fun filterElementsByType(elements: Array<ASTBaseNode>, type: dynamic): List<dynamic> {
        return elements.filter { it.type == type }
    }
}


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

    val placeToInitialMarking: MutableMap<String, Int> = mutableMapOf()
    val placeToObjectType: MutableMap<String, String> = mutableMapOf()
    val inputPlaceLabels: MutableList<String> = mutableListOf()
    val outputPlaceLabels: MutableList<String> = mutableListOf()

    val savedObjectTypes: Map<String, Node>
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

    fun rememberInitialMarkingForPlace(placeLabel: String, initialTokens: Int) {
        console.log("saving $placeLabel initial tokens $initialTokens")
        placeToInitialMarking[placeLabel] = initialTokens
    }

    fun rememberPlaceIsInput(placeLabel: String) {
        console.log("remembered input $placeLabel")
        inputPlaceLabels.add(placeLabel)
    }

    fun recallIfPlaceIsInput(placeLabel: String): Boolean {
        val input = inputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is input $input")
        return input
    }

    fun recallIfPlaceIsOutput(placeLabel: String): Boolean {
        val output = outputPlaceLabels.find { it == placeLabel } != null
        console.log("place $placeLabel is output $output")
        return output
    }

    fun rememberPlaceIsOutput(placeLabel: String) {
        console.log("remembered output $placeLabel")
        outputPlaceLabels.add(placeLabel)
    }

    fun rememberObjectTypeForPlace(placeLabel: String, objectTypeLabel: String) {
        placeToObjectType[placeLabel] = objectTypeLabel
    }

    fun rememberSubgraph(subgraph: Subgraph) {
        subgraphAssociations.rememberSubgraph(subgraph)
    }

    fun rememberObjectType(objectType: Node) {
        objectTypes[objectType.id.value] = objectType
    }

    fun recallObjectTypeForPlace(placeLabel: String): String? {
        return placeToObjectType[placeLabel]
    }

    fun recallInitialTokensForPlace(placeLabel: String): Int? {
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

class SemanticDomainErrorReporterContainer() : ErrorReporterContainer {
    private val collectedErrors = mutableListOf<Error>()

    fun pushError(error: Error) {
        collectedErrors.add(error)
    }

    override fun collectReport(): List<Error> {
        return collectedErrors
    }
}

interface ErrorReporterContainer {
    fun collectReport(): List<Error>
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
    private val errorReporterContainer: SemanticDomainErrorReporterContainer,
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
    private val errorReporterContainer: SemanticDomainErrorReporterContainer,
) : ChainStage<DSLElementsContainer, OCDotParseResult.Success>(
    DSLElementsContainer::class,
    OCDotParseResult.Success::class
) {
    override fun perform(input: DSLElementsContainer): ChainResult<OCDotParseResult.Success> {
        val ocDotToDomainConverter = OCDotToDomainConverter()

        val result = ocDotToDomainConverter.convert(input)
        console.log(result)

        if (result.hasCriticalErrors) {
            return ChainResult(
                failure = OCDotParseResult.DomainCheckCriticalErrorsFound(
                    message = "Domain checks failed for the given net",
                    collectedSemanticErrors = result.errors
                )
            )
        } else {
            return ChainResult(
                success = OCDotParseResult.Success(result)
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

@OptIn(ExperimentalJsExport::class)
@JsExport
actual class OCDotParser {

    private val ocScopeImpl = OCScopeImplCreator().createRootOCScope()
    private val dslElementsContainer = DSLElementsContainer(ocScopeImpl)
    private val errorReporterContainer = SemanticDomainErrorReporterContainer()

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

//        this.result = (result as? OCDotParseResult.Success)?.buildOCNet

        return result
    }
}

object JsToCommonMapping {
    fun filePosition(filePosition: FilePosition): FilePosition {
        return filePosition
    }

    fun fileRange(fileRange: FileRange): FileRange {
        return fileRange
    }
}
