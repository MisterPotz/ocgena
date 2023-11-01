package converter

interface SemanticChecker{
    fun checkErrors(objectHolder: ObjectHolder) : Boolean
}
