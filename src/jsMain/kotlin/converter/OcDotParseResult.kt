package converter

import error.Error
import model.StaticCoreOcNet

@JsExport
class OcDotParseResult(
    val ocNet: StaticCoreOcNet? = null,
    val errors: Array<Error>,
) {
}
