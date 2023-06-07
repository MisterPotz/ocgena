package converter

import converter.visitors.DelegateOCDotASTVisitorBFS
import converter.visitors.ElementsDeclarationsASTVisitorBFS
import converter.visitors.StructureCheckASTVisitorBFS

actual class SemanticCheckerCreator actual constructor(
    private val destStructureContainer: StructureContainer,
    private val errorReporterContainer: ErrorReporterContainer,
) {
    actual fun create(): SemanticChecker {
        val structureCheckerVisitor = StructureCheckASTVisitorBFS(errorReporterContainer)
        val elementsDeclarationsVisitor =
            ElementsDeclarationsASTVisitorBFS(destStructureContainer, errorReporterContainer)

        val delegateOCDotASTVisitorBFS = DelegateOCDotASTVisitorBFS(
            listOf(
                structureCheckerVisitor,
                elementsDeclarationsVisitor,
            )
        )

        return SemanticCheckerImpl(
            delegateOCDotASTVisitorBFS, errorReporterContainer
        )
    }
}
