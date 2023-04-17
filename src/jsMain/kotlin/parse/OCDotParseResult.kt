package parse

import ast.ASTBaseNode
import error.ConsistencyCheckError
import error.Error
import error.ErrorLevel

data class SemanticParseException(
    override val message: String,
    override val errorLevel: ErrorLevel,
    val originalException: Throwable?,
) : Error

data class SemanticErrors(
    override val message: String,
    val collectedSemanticErrors: List<SemanticError>,
    override val errorLevel: ErrorLevel,
) : Error

data class SemanticError(
    override val message: String,
    val relatedAst: ASTBaseNode,
    override val errorLevel: ErrorLevel,
): Error

data class DomainModelErrors(
    override val message: String,
    val errors: List<ConsistencyCheckError>,
    override val errorLevel: ErrorLevel,
) : Error
