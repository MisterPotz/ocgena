package converter

expect class SemanticCheckerCreator(
    structureContainer: StructureContainer,
    errorReporterContainer: ErrorReporterContainer
) {
    fun create() : SemanticChecker
}
