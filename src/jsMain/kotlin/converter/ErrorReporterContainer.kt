package converter

import error.Error

interface ErrorReporterContainer {

    fun pushError(error: Error)
    fun collectReport(): List<Error>
}
