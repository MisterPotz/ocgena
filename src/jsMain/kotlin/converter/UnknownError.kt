package converter

import error.Error
import error.ErrorLevel

class UnknownError(override val message: String, override val errorLevel: ErrorLevel) : Error
