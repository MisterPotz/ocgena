package ru.misterpotz.ocgena.testing

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
