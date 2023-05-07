package converter

import error.Error

class DefaultErrorReporterContainer() : ErrorReporterContainer {
    private val collectedErrors = mutableListOf<Error>()

    override fun pushError(error: Error) {
        collectedErrors.add(error)
    }

    override fun collectReport(): List<Error> {
        return collectedErrors
    }
}
