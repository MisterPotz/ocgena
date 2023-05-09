package converter

import declarations.ocdot.PeggySyntaxError
import error.Error
import error.ErrorLevel

class PeggySyntaxErrorWrapper(peggySyntaxError: PeggySyntaxError) : Error {
    override val message: String = buildPeggyErrorMessage(peggySyntaxError)
    override val errorLevel: ErrorLevel = ErrorLevel.CRITICAL


    private fun buildPeggyErrorMessage(peggySyntaxError: PeggySyntaxError): String {
        return PeggySyntaxError.buildMessage(peggySyntaxError.expected, peggySyntaxError.found)
    }
}
