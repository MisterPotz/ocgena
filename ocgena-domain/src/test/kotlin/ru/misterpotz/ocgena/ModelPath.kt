package ru.misterpotz.ocgena

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

enum class ModelPath(val path: Path) {
    ONE_IN_TWO_OUT(Path("nets") / "one_in_two_out.yaml"),
    TWO_IN_TWO_OUT(Path("nets") / "two_in_two_out_var.yaml"),
    ONE_IN_TWO_MIDDLE(Path("nets") / "one_in_two_middle.yaml"),
    AALST(Path("nets") / "aalst.yaml")
}

fun ModelPath.load(): OCNetStruct {
    return readConfig(path)
}