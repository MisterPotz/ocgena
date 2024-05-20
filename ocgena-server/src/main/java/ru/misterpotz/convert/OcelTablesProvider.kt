package ru.misterpotz.convert

object OcelTablesProvider {
    val eventsTable: EventsTable = EventsTable
    val eventObjectsTable: EventObjectsTable = EventObjectsTable
    val objectsTable: ObjectsTable = ObjectsTable
    val objectObjectsTable: ObjectObjectsTable = ObjectObjectsTable

    private val eventTables: MutableMap<String, ConcreteEventTable> = mutableMapOf()
    private val objectTables: MutableMap<String, ConcreteObjectTable> = mutableMapOf()

    fun concreteEventTable(tableName: String): ConcreteEventTable {
        return eventTables.getOrPut(tableName) {
            ConcreteEventTable(tableName)
        }
    }

    fun concreteObjectTable(tableName : String): ConcreteObjectTable {
        return objectTables.getOrPut(tableName) {
            ConcreteObjectTable(tableName)
        }
    }

    val eventTypeMap: EventTypeMapTable = EventTypeMapTable
    val objectTypeMap: ObjectTypeMapTable = ObjectTypeMapTable
}