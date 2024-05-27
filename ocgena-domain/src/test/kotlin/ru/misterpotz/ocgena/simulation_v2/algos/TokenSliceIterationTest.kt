package ru.misterpotz.ocgena.simulation_v2.algos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.utils.defaultObjType
import ru.misterpotz.ocgena.simulation_v2.entities.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.toStringSnapshot

class TokenSliceIterationTest {

    private fun makeTokenSlice(): TokenSlice {
        val objType = defaultObjType

        val token1 = TokenWrapper(1, objectType = objType, mutableSetOf(1, 2, 5, 10))
        val token2 = TokenWrapper(2, objType, mutableSetOf(2, 5, 9))
        val token3 = TokenWrapper(3, objType, mutableSetOf(3, 4, 5, 9, 10, 20))
        val token11 = TokenWrapper(11, objType, mutableSetOf(12, 15))
        val token12 = TokenWrapper(12, objType, mutableSetOf(10, 12, 14))
        val token13 = TokenWrapper(13, objType, mutableSetOf(20))

        val p1 = PlaceWrapper("p1", objType)
        val p2 = PlaceWrapper("p2", objType)
        val p3 = PlaceWrapper("p3", objType)

        val tokenSlice = SimpleTokenSlice.build {
            addTokens(p1, listOf(token1, token11))
            addTokens(p2, listOf(token2, token12))
            addTokens(p3, listOf(token3, token13))
        }
        return tokenSlice
    }

    @Test
    fun tokenSliceIterationByHistoryTest() {
        val tokenSlice = makeTokenSlice()
        val iterator = tokenSlice.iterateByCommonHistoryEntries(emptySet(), deepCopy = true)
        val expectedOutputSnapshot = """
            historical token slice, entry: 1
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:0) ->  []
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 2
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [2[△0]]
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 3
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:0) ->  []
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 4
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:0) ->  []
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 5
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [2[△0]]
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 9
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:1) ->  [2[△0]]
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 10
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [12[△0]]
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 12
                tokensmap:
                    p1 (size:1) ->  [11[△0]]
                    p2 (size:1) ->  [12[△0]]
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 14
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:1) ->  [12[△0]]
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 15
                tokensmap:
                    p1 (size:1) ->  [11[△0]]
                    p2 (size:0) ->  []
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 20
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:0) ->  []
                    p3 (size:2) ->  [3[△0],13[△0]]

        """.trimIndent()

        Assertions.assertEquals(expectedOutputSnapshot, iterator.asSequence().toList().toStringSnapshot())
    }

    @Test
    fun exclusionRules() {
        val tokenSlice = makeTokenSlice()
        val iterator = tokenSlice.iterateByCommonHistoryEntries(setOf(3, 9, 12, 20), deepCopy = true)

        val expectedOutputSnapshot = """
            historical token slice, entry: 1
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:0) ->  []
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 2
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [2[△0]]
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 4
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:0) ->  []
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 5
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [2[△0]]
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 10
                tokensmap:
                    p1 (size:1) ->  [1[△0]]
                    p2 (size:1) ->  [12[△0]]
                    p3 (size:1) ->  [3[△0]]
            -------------
            historical token slice, entry: 14
                tokensmap:
                    p1 (size:0) ->  []
                    p2 (size:1) ->  [12[△0]]
                    p3 (size:0) ->  []
            -------------
            historical token slice, entry: 15
                tokensmap:
                    p1 (size:1) ->  [11[△0]]
                    p2 (size:0) ->  []
                    p3 (size:0) ->  []

        """.trimIndent()
        Assertions.assertEquals(expectedOutputSnapshot, iterator.asSequence().toList().toStringSnapshot())
    }
}