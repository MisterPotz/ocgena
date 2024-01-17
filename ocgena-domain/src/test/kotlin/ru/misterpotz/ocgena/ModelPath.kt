package ru.misterpotz.ocgena

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

enum class ModelPath(val path: Path) {
    ONE_IN_TWO_OUT(Path("common/nets") / "one_in_two_out.yaml"),
    TWO_IN_TWO_OUT(Path("common/nets") / "two_in_two_out_var.yaml"),
    ONE_IN_TWO_MIDDLE(Path("common/nets") / "one_in_two_middle.yaml"),
    THREE_IN_TWO_OUT(Path("common/nets") / "three_in_two_out.yaml"),
    AALST(Path("common/nets") / "aalst.yaml")
}

fun ModelPath.load(): OCNetStruct {
    return readConfig(path)
}