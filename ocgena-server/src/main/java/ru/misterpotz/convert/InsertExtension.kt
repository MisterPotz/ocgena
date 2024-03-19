package ru.misterpotz.convert

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import ru.misterpotz.SimulationGeneralData
import ru.misterpotz.models.SimulationDBStepLog

interface InsertExtension {
    val ocelTablesProvider: OcelTablesProvider
    val db: Database
    val labelConverter: LabelConverter
    val simulationGeneralData: SimulationGeneralData

    fun insertLog(log: SimulationDBStepLog) {
        insertEvent(log)
    }

    fun insertEvent(log: SimulationDBStepLog) {
        if (log.selectedFiredTransition == null) return

        with(ocelTablesProvider) {
            eventsTable.insert {
                val ocelEventId = labelConverter.ocelEventId(log)
                it[eventsTable.ocelEventId] = ocelEventId
                it[eventsTable.ocelEventType] =
                    simulationGeneralData.transitionIdToLabel[log.selectedFiredTransition.transitionId]!!
            }
        }
    }

    fun insertRelatedObjects(log: SimulationDBStepLog) {
        with(ocelTablesProvider) {
            val eventId = labelConverter.ocelEventId(log)
            // TODO: insert related objects here
        }
    }
}

internal class InsertExtensionImpl @AssistedInject constructor(
    @Assisted
    override val labelConverter: LabelConverter,
    @Assisted
    override val db: Database,
    @Assisted
    override val simulationGeneralData: SimulationGeneralData,
    override val ocelTablesProvider: OcelTablesProvider,
) : InsertExtension

@AssistedFactory
internal interface InsertExtensionAssistedFactory {
    fun create(
        labelConverter: LabelConverter,
        db: Database,
        simulationGeneralData: SimulationGeneralData
    ): InsertExtensionImpl
}