package dsl

import model.PlaceType

class OCNetBuilder {
    fun define(block: OCScope.() -> Unit): OCScopeImpl {
        val oCScopeImpl = OCScopeImpl()
        oCScopeImpl.block()

        return oCScopeImpl
    }
}
