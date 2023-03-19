package dsl

import converter.OCNetElements
import model.ConsistencyCheckError
import model.OCNet
import model.OCNetChecker
import model.OCNetDSLConverter

class OCNetFacadeBuilder {
    var definedNetData: DefinedNetData? = null
        private set

    val errors: List<ConsistencyCheckError>?
        get() = definedNetData?.errors

    class BuiltOCNet(
        val ocNetElements: OCNetElements,
        val errors: List<ConsistencyCheckError>,
        val ocNet: OCNet?,
    ) {
        fun requireConsistentOCNet(): OCNet {
            return ocNet!!
        }
    }

    fun tryBuildModel(block: OCScope.() -> Unit): BuiltOCNet {
        val ocScope = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocScope)
        val convertionResult = converter.convert()
        val ocNetChecker = OCNetChecker(convertionResult.allPetriNodes)

        val errors = ocNetChecker.checkConsistency()
        val ocNet = if (ocNetChecker.isConsistent) ocNetChecker.createConsistentOCNet() else null

        val definedNetData = DefinedNetData(
            ocNet = ocNet,
            errors = errors,
        )
        this.definedNetData = definedNetData
        return BuiltOCNet(
            ocNetElements = convertionResult,
            errors = errors,
            ocNet = ocNet
        )
    }
}

class DefinedNetData(
    val ocNet: OCNet?,
    val errors: List<ConsistencyCheckError>,
)
