package dsl

import error.ConsistencyCheckError
import error.ErrorLevel
import model.*
import model.utils.OCNetDSLConverter
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.ocgena.validation.OCNetChecker

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
        placeObjectTypeRegistry: PlaceObjectTypeRegistry,
        placeTypeRegistry: PlaceTypeRegistry,
        block: OCScope.() -> Unit): BuiltOCNet {
        val ocNetDSLElements = OCNetBuilder.define(block)
        val converter = OCNetDSLConverter(ocNetDSLElements, placeObjectTypeRegistry)
        val convertionResult = converter.convert()
        val ocNetChecker = OCNetChecker(convertionResult, placeObjectTypeRegistry, placeTypeRegistry)

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
        placeObjectTypeRegistry: PlaceObjectTypeRegistry,
        placeTypeRegistry: PlaceTypeRegistry,
        ocNetElements: OCNetElements
    ) : BuiltOCNet {
        val ocNetChecker = OCNetChecker(ocNetElements, placeObjectTypeRegistry, placeTypeRegistry)
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
