package ru.misterpotz.ocgena.dsl

class OCNetBuilder {
    companion object {
        fun define(block: OCScope.() -> Unit): OCNetDSLElements {
            throw NotImplementedError(
                "after refactoring in tech/extract-kotlin branch implementation was removed," +
                        " create new and better class"
            )
        }
    }
}
