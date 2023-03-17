package dsl

class UnresolvedHasLast(var resolvedLastElement: NodeDSL? = null) : HasLast {
    override val lastElement: NodeDSL
        get() = resolvedLastElement!!
}
