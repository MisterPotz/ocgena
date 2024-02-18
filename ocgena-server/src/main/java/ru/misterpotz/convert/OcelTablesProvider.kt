package ru.misterpotz.convert

import javax.inject.Inject

interface OcelTablesProvider {
    val eventsTable: EventsTable
    val eventObjectsTable: EventObjectsTable
    val objectsTable: ObjectsTable
    val objectObjectsTable: ObjectObjectsTable
    fun concreteEventTable(eventMapType: String): ConcreteEventTable
    fun concreteObjectTable(objectMapType: String): ConcreteObjectTable
    val eventTypeMap: EventTypeMapTable
    val objectTypeMap: ObjectTypeMapTable
}

internal class OcelTablesProviderImpl @Inject constructor() : OcelTablesProvider {
    override val eventsTable: EventsTable = EventsTable()
    override val eventObjectsTable: EventObjectsTable = EventObjectsTable()
    override val objectsTable: ObjectsTable = ObjectsTable()
    override val objectObjectsTable: ObjectObjectsTable = ObjectObjectsTable()

    private val eventTables: MutableMap<String, ConcreteEventTable> = mutableMapOf()
    private val objectTables: MutableMap<String, ConcreteObjectTable> = mutableMapOf()

    override fun concreteEventTable(eventMapType: String): ConcreteEventTable {
        return eventTables.getOrPut(eventMapType) {
            ConcreteEventTable(ConcreteEventTable.createTableName(eventMapType))
        }
    }

    override fun concreteObjectTable(objectMapType: String): ConcreteObjectTable {
        return objectTables.getOrPut(objectMapType) {
            ConcreteObjectTable(ConcreteObjectTable.createTableName(objectMapType))
        }
    }

    override val eventTypeMap: EventTypeMapTable = EventTypeMapTable()
    override val objectTypeMap: ObjectTypeMapTable = ObjectTypeMapTable()
}