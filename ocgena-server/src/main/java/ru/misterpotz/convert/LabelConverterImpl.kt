package ru.misterpotz.convert

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.misterpotz.SimulationGeneralData
import ru.misterpotz.models.SimulationDBStepLog

class LabelConverterImpl @AssistedInject constructor(@Assisted private val simulationGeneralData: SimulationGeneralData) :
    LabelConverter {
    override fun ocelEventId(
        simulationDBStepLog: SimulationDBStepLog,
    ): String {
        val transitionId = simulationDBStepLog.selectedFiredTransition!!.transitionId
        val transitionLabel = simulationGeneralData.transitionIdToLabel[transitionId]!!
        val step = simulationDBStepLog.stepNumber

        return "${transitionLabel.toId()}_$step"
    }

    override fun ocelObjectId(simulationDBStepLog: SimulationDBStepLog, tokenId: Long): String {
        val objectTypeId = simulationDBStepLog.tokenIdToObjectTypeId[tokenId]!!
        val objectTypeIdLabel = simulationGeneralData.objectTypeIdToLabel[objectTypeId]!!
        return "${objectTypeIdLabel.toId()}_$tokenId"
    }

    private fun String.toId(): String {
        return split(" ").joinToString(separator = "") { it.first().lowercase() }
    }

    private fun String.toMapType(): String {
        return split(" ").joinToString(separator = "") { it.replaceFirstChar { it.uppercase() } }
    }

    override fun eventLabelToMapType(eventLabel: String): String {
        return eventLabel.toMapType()
    }

    override fun objectTypeLabelToMapType(objectLabel: String): String {
        return objectLabel.toMapType()
    }
}

@AssistedFactory
interface LabelConverterAssistedFactory {
    fun create(simulationGeneralData: SimulationGeneralData): LabelConverterImpl
}