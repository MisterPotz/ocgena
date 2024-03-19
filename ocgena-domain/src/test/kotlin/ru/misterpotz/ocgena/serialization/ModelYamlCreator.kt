package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.testing.OCNetBuildingCodeBlock
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.testing.buildingBlockTwoInTwoOutMiddle
import ru.misterpotz.ocgena.testing.installOnto

/**
 * Images are here
 * <img src="./doc-files/img.png" >
 */
class ModelYamlCreator {

    @Test
    fun oneInTwoOut() {
        val ocNet = buildOCNet {
            "p1".p { objectTypeId = "o1"; input }
            "p2".p { objectTypeId = "o1"; output }
            "p3".p { objectTypeId = "o2"; output }
            "t1".t

            "p1".arc("t1") { multiplicity = 2 }
                .also {
                    it.arc("p2") { multiplicity = 4 }
                    it.arc("p3") { multiplicity = 1 }
                }
        }

        writeOrAssertYaml(ocNet, ModelPath.ONE_IN_TWO_OUT.path)
    }

    @Test
    fun twoInTwoOutVar() {
        val ocNet = buildOCNet {
            buildTwoInTwoOut().installOnto(this)
        }

        writeOrAssertYaml(ocNet, ModelPath.TWO_IN_TWO_OUT.path)
    }

    @Test
    fun twoInTwoOutMiddle() {
        val ocNet = buildOCNet {
            buildingBlockTwoInTwoOutMiddle().installOnto(this)
        }
        writeOrAssertYaml(ocNet, ModelPath.TWO_IN_TWO_OUT_MIDDLE.path)
    }

    @Test
    fun threeInTwoOut() {
        val ocNet = buildOCNet {
            "p1".p { input; objectTypeId = "1" }
                .arc("t1".t)
                .arc("p2".p { output; objectTypeId = "1" })

            "o1".p { input; objectTypeId = "2" }
                .arc("t1".t) { vari }
                .arc("o2".p { output; objectTypeId = "2" }) { vari }

            "p3".p { input; objectTypeId = "1" }
                .arc("t1".t) { norm; multiplicity = 3 }
        }

        writeOrAssertYaml(ocNet, ModelPath.THREE_IN_TWO_OUT.path)

    }

    @Test
    fun aalst() {
        val ocnet = createExampleModel()
        writeOrAssertYaml(ocnet, ModelPath.AALST.path)
    }

    companion object {
        fun buildTwoInTwoOut() : OCNetBuildingCodeBlock {
            return {
                "p1".p { input; objectTypeId = "1" }
                    .arc("t1".t)
                    .arc("p2".p { output; objectTypeId = "1" })

                "o1".p { input; objectTypeId = "2" }
                    .arc("t1".t) { vari }
                    .arc("o2".p { output; objectTypeId = "2" }) { vari }

            }
        }
    }
}