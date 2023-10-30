package dsl

class OCNetBuilder {
    companion object {
        fun define(block: OCScope.() -> Unit): OCNetDSLElements {
            val ocScopeCreator = OCScopeImplCreator()

            val oCScopeImpl = ocScopeCreator.createRootOCScope()
            oCScopeImpl.block()
            return oCScopeImpl.ocNetElements()
        }
    }
}
