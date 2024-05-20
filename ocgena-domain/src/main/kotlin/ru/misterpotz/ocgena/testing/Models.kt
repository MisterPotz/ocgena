package ru.misterpotz.ocgena.testing

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType

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

fun buildSynchronizingLomazovaExampleModel() = buildOCNet(OcNetType.LOMAZOVA) {
    "order".p { input; objectTypeId = "1" }
    "package".p { input; objectTypeId = "2" }
    "track".p { input; objectTypeId = "3" }

    "place order".t
    "order".arc("place order")
    "package".arc("place order") { vari; mathExpr = "m" }

    "place order".arc("o2".p { objectTypeId = "1" })
    "place order".arc("p2".p { objectTypeId = "2" }) { vari; mathExpr = "m" }

    "p2".arc("arrange packages to tracks".t) { vari; mathExpr = "2*n" }
    "track".arc("arrange packages to tracks") { vari; mathExpr = "n" }
    "arrange packages to tracks".apply {
        arc("p3".p { objectTypeId = "2" }) { vari; mathExpr = "2*n" }
        arc("t2".p { objectTypeId = "3" }) { vari; mathExpr = "n" }
    }

    "bill".p { input; objectTypeId = "4" }
        .arc("send invoices".t) { vari; mathExpr = "k" }
        .arc("b2".p { objectTypeId = "4" }) { vari; mathExpr = "k" }
    "o2".p.arc("send invoices".t)
    "send invoices".arc("o3".p { objectTypeId = "1" })

    "test all sync".t

    "o3".arc("test all sync") { vari; mathExpr = "o" }
    "b2".arc("test all sync") { vari; mathExpr = "2*o" }
    "p3".arc("test all sync") { vari; mathExpr = "2*o" }
    "t2".arc("test all sync") { vari; mathExpr = "t" }

    "test all sync".arc("output".p { objectTypeId = "1"; output }) { vari; mathExpr = "o" }
}