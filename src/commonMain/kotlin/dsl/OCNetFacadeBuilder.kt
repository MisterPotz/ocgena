package dsl

import error.ConsistencyCheckError
import error.ErrorLevel
import model.*
import model.utils.OCNetDSLConverter

class OCNetFacadeBuilder {
    var definedNetData: DefinedNetData? = null
        private set

    val errors: List<ConsistencyCheckError>?
        get() = definedNetData?.errors

    class BuiltOCNet(
        val errors: List<ConsistencyCheckError>,
        val ocNet: StaticCoreOcNet?,
    ) {
        val hasCriticalErrors
            get() =  errors.find { it.errorLevel == ErrorLevel.CRITICAL } != null

        fun requireConsistentOCNet(): StaticCoreOcNet {
            return ocNet!!
        }
    }

    fun tryBuildModelFromDSl(
        placeTyping: PlaceTyping,
        inputOutputPlaces: InputOutputPlaces,
        block: OCScope.() -> Unit): BuiltOCNet {
        val ocNetDSLElements = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocNetDSLElements, placeTyping)
        val convertionResult = converter.convert()
        val ocNetChecker = OCNetChecker(convertionResult, placeTyping, inputOutputPlaces)

        val errors = ocNetChecker.checkConsistency()
        val ocNet = if (ocNetChecker.isConsistent) ocNetChecker.createWellFormedOCNet() else null

        val definedNetData = DefinedNetData(
            ocNet = ocNet,
            errors = errors,
        )
        this.definedNetData = definedNetData
        return BuiltOCNet(
            errors = errors,
            ocNet = ocNet,
        )
    }

    fun tryBuildModelFromOcNetElements(
        placeTyping: PlaceTyping,
        inputOutputPlaces: InputOutputPlaces,
        ocNetElements: OCNetElements
    ) : BuiltOCNet {
        val ocNetChecker = OCNetChecker(ocNetElements, placeTyping, inputOutputPlaces)
        val errors = ocNetChecker.checkConsistency()
        val ocNet = if (ocNetChecker.isConsistent) ocNetChecker.createWellFormedOCNet() else null
        val definedNetData = DefinedNetData(ocNet = ocNet, errors = errors)
        this.definedNetData = definedNetData
        return BuiltOCNet(
            errors = errors,
            ocNet = ocNet
        )
    }
}

class DefinedNetData(
    val ocNet: StaticCoreOcNet?,
    val errors: List<ConsistencyCheckError>,
)
