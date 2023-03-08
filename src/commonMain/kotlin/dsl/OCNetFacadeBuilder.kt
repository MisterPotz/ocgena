package dsl

import model.ConsistencyCheckError
import model.OCNet
import model.OCNetChecker
import model.OCNetDSLConverter

class OCNetFacadeBuilder {
    var definedNetData : DefinedNetData? = null
        private set

    val errors : List<ConsistencyCheckError>?
        get() = definedNetData?.errors
    fun tryBuildModel(block: OCScope.() -> Unit) : OCNet? {
        val ocScope = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocScope)
        val convertionResult = converter.convert()
        val ocNetChecker = OCNetChecker(convertionResult.allPetriNodes)

        val errors = ocNetChecker.checkConsistency()
        val ocNet = if (ocNetChecker.isConsistent) ocNetChecker.createConsistentOCNet() else null

        val definedNetData = DefinedNetData(
            ocNet = ocNet,
            errors = errors,
            ocNetOCScopeImpl = ocScope
        )
        this.definedNetData = definedNetData
        return definedNetData.ocNet
    }
}

class DefinedNetData(
    val ocNet: OCNet?,
    val errors: List<ConsistencyCheckError>,
    val ocNetOCScopeImpl: OCScopeImpl,
)