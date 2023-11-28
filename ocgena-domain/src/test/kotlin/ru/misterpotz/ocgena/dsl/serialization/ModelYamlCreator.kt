package ru.misterpotz.ocgena.dsl.serialization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.tool.buildOCNet
import ru.misterpotz.ocgena.dsl.tool.readConfig
import ru.misterpotz.ocgena.dsl.tool.toYaml
import ru.misterpotz.ocgena.dsl.tool.writeConfig
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import kotlin.io.path.Path
import kotlin.io.path.div

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

        val recordedConfig = readConfig<OCNetStruct>(Path("nets") / "one_in_two_out.yaml")

        Assertions.assertEquals(recordedConfig, ocNet)
//        val yaml = ocNet.toYaml()
//        yaml.writeConfig(Path("nets") / "one_in_two_out.yaml")
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

        val serialized = readConfig<OCNetStruct>(Path("nets") / "two_in_two_out_var.yaml")
        Assertions.assertEquals(ocNet, serialized)
//        val yaml = ocNet.toYaml()
//        yaml.writeConfig(Path("nets") / "two_in_two_out_var.yaml")
    }

    @Test
    fun oneInTwoMiddle() {
        val ocNet = buildOCNet {
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

        val serialized = readConfig<OCNetStruct>(Path("nets") / "one_in_two_middle.yaml")
        Assertions.assertEquals(ocNet, serialized)
//        val yaml = ocNet.toYaml()
//        yaml.writeConfig(Path("nets") / "one_in_two_middle.yaml")
    }
}