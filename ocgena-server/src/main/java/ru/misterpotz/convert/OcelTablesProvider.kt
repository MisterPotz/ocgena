package ru.misterpotz.convert

object OcelTablesProvider {
    val eventsTable: EventsTable = EventsTable()
    val eventObjectsTable: EventObjectsTable = EventObjectsTable()
    val objectsTable: ObjectsTable = ObjectsTable()
    val objectObjectsTable: ObjectObjectsTable = ObjectObjectsTable()

    private val eventTables: MutableMap<String, ConcreteEventTable> = mutableMapOf()
    private val objectTables: MutableMap<String, ConcreteObjectTable> = mutableMapOf()

    fun concreteEventTable(eventMapType: String): ConcreteEventTable {
        return eventTables.getOrPut(eventMapType) {
            ConcreteEventTable(ConcreteEventTable.createTableName(eventMapType))
        }
    }

    fun concreteObjectTable(objectMapType: String): ConcreteObjectTable {
        return objectTables.getOrPut(objectMapType) {
            ConcreteObjectTable(ConcreteObjectTable.createTableName(objectMapType))
        }
    }

    val eventTypeMap: EventTypeMapTable = EventTypeMapTable()
    val objectTypeMap: ObjectTypeMapTable = ObjectTypeMapTable()
}