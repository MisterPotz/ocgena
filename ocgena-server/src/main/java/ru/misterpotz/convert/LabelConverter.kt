package ru.misterpotz.convert

import ru.misterpotz.models.SimulationDBStepLog

interface LabelConverter {
    fun ocelEventId(simulationDBStepLog: SimulationDBStepLog): String
    fun ocelObjectId(simulationDBStepLog: SimulationDBStepLog, tokenId: Long): String
    fun eventLabelToMapType(eventLabel: String): String
    fun objectTypeLabelToMapType(objectLabel: String): String
}