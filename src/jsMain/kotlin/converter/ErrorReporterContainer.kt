package converter

import error.Error

interface ErrorReporterContainer {
    fun collectReport(): List<Error>
}
