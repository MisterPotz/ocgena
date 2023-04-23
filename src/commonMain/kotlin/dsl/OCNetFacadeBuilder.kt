package dsl

import error.ConsistencyCheckError
import error.ErrorLevel
import model.OCNetChecker
import model.OCNetElements
import model.StaticCoreOcNet
import model.utils.OCNetDSLConverter

class OCNetFacadeBuilder {
    var definedNetData: DefinedNetData? = null
        private set

    val errors: List<ConsistencyCheckError>?
        get() = definedNetData?.errors

    class BuiltOCNet(
        val ocNetElements: OCNetElements,
        val errors: List<ConsistencyCheckError>,
        val ocNet: StaticCoreOcNet?,
    ) {
        val hasCriticalErrors
            get() =  errors.find { it.errorLevel == ErrorLevel.CRITICAL } != null

        fun requireConsistentOCNet(): StaticCoreOcNet {
            return ocNet!!
        }
    }

    fun tryBuildModel(block: OCScope.() -> Unit): BuiltOCNet {
        val ocNetDSLElements = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocNetDSLElements)
        val convertionResult = converter.convert()
        val ocNetChecker = OCNetChecker(convertionResult)

        val errors = ocNetChecker.checkConsistency()
        val ocNet = if (ocNetChecker.isConsistent) ocNetChecker.createWellFormedOCNet() else null

        val definedNetData = DefinedNetData(
            ocNet = ocNet,
            errors = errors,
        )
        this.definedNetData = definedNetData
        return BuiltOCNet(
            ocNetElements = convertionResult,
            errors = errors,
            ocNet = ocNet,
        )
    }
}

class DefinedNetData(
    val ocNet: StaticCoreOcNet?,
    val errors: List<ConsistencyCheckError>,
)
