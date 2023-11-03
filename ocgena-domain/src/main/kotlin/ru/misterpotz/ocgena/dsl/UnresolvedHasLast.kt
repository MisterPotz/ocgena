package ru.misterpotz.ocgena.dsl

class UnresolvedHasLast(var resolvedLastElement: NodeDSL? = null) : HasLast {
    override val lastElement: NodeDSL
        get() = resolvedLastElement!!
}
