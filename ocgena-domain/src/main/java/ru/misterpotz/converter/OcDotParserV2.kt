package converter

expect class OcDotParserV2(
    errorReporterContainer: ErrorReporterContainer
) {
    fun parse(ocDot: String): Result<ObjectHolder>
}
