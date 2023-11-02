package error

interface Error {
    val message: String
    val errorLevel : ErrorLevel
}

class ErrorClass(override val message: String, override val errorLevel: ErrorLevel) : Error

enum class ErrorLevel {
    WARNING,
    CRITICAL
}
