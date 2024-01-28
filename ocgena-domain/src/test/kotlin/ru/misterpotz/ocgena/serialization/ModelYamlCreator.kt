package ru.misterpotz.ocgena.serialization

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*

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
            "p1".p { input; objectTypeId = "1" }
                .arc("t1".t)
                .arc("p2".p { output; objectTypeId = "1" })

            "o1".p { input; objectTypeId = "2" }
                .arc("t1".t) { vari }
                .arc("o2".p { output; objectTypeId = "2" }) { vari }
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
        fun buildingBlockTwoInTwoOutMiddle(): OCNetBuildingCodeBlock {
            return {
                "p1".p { input; objectTypeId = "1" }
                    .arc("t1".t)
                    .arc("p2".p { objectTypeId = "1" })

                "t1".arc("o1".p { objectTypeId = "2" })

                "p2".arc("t2".t)
                    .arc("p3".p { objectTypeId = "1"; output })

                "p2".arc("t3".t)

                "o1".arc("t3".t) { vari; }
                    .arc("o2".p { objectTypeId = "2"; output }) { vari; }

                "t3".arc("p3") { norm; multiplicity = 0 }
            }
        }
    }
}