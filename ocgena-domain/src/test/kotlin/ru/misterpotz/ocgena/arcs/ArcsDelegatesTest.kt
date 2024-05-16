package ru.misterpotz.ocgena.arcs

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.ocnet.utils.makeObjTypeId
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SparseTokenBunchImpl

class ArcsDelegatesTest {
    @Test
    fun `for normal arcs arc delegate dynamic works correct`() {

        val (bunch, registry) = SparseTokenBunchImpl.makeBuilder {
            forPlace("p1") {
                realTokens = 4
                type = "p".makeObjTypeId()
            }
            forPlace("o1") {
                realTokens = 2
                type = "o".makeObjTypeId()
            }
            forPlace("z1") {
                realTokens = 5
                type = "z".makeObjTypeId()
            }
        }.buildWithTypeRegistry()

        val arcToMultiplicityNormalDelegateTypeA = ArcToMultiplicityNormalDelegateTypeA(
            sparseTokenBunch = bunch,
            registry
        )

        val multiplicityDynamic = arcToMultiplicityNormalDelegateTypeA.transitionInputMultiplicityDynamic(
            NormalArc(id = "p1".arcIdTo("t"), multiplicity = 1)
        )
        Assertions.assertTrue(multiplicityDynamic.inputPlaceHasEnoughTokens(bunch.tokenAmountStorage))
        Assertions.assertTrue(multiplicityDynamic.requiredTokenAmount(bunch.tokenAmountStorage) == 1)
    }
}