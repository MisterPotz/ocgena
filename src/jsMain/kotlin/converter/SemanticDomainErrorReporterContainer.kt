package converter

import error.Error

class SemanticDomainErrorReporterContainer() : ErrorReporterContainer {
    private val collectedErrors = mutableListOf<Error>()

    fun pushError(error: Error) {
        collectedErrors.add(error)
    }

    override fun collectReport(): List<Error> {
        return collectedErrors
    }
}
