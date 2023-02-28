package model

import dsl.OCScopeImpl
import dsl.createExampleModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OCNetCheckerTest {
    private var _ocScopeImpl: OCScopeImpl? = null
    val ocScopeImpl
        get() = _ocScopeImpl!!

    init {
        _ocScopeImpl = createExampleModel()
    }

    @Test
    fun testModelIsConsistent() {

    }
}
