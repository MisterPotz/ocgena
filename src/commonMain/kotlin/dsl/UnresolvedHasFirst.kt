package dsl

class UnresolvedHasFirst(var resolvedFirstElement: NodeDSL? = null) : HasFirst {
    override val firstElement: NodeDSL
        get() = resolvedFirstElement!!

}
