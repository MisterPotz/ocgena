package converter

expect class SemanticCheckerCreator(
    destStructureContainer: StructureContainer,
    errorReporterContainer: ErrorReporterContainer
) {
    fun create() : SemanticChecker
}
