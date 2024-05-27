package ru.misterpotz.ocgena.testing

fun build3Tran4InpExample() = buildOCNet {
    "input1".p { input }
    "input2".p { input }
    "input3".p { input }
    "input4".p { input; }
    "t2".t
    "input1".arc("t2")
    "input2".arc("t2")
    "p1".p { objectTypeId = "1" }
    "t2".arc("p1")
    "p1".arc("t1".t) { vari; }
    "p2".p
    "input3".arc("t3".t).arc("p2")
    "t2".arc("p2")
    "p2".arc("t1")
    "t1".t.arc("output".p { output })
    "input4".arc("t1")
}