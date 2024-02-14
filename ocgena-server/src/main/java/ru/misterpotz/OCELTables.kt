package ru.misterpotz

import org.jetbrains.exposed.sql.Table

class EventsTable : Table("event") {
    val ocelEventId = text("ocel_id")
    val ocelEventType = text("ocel_type")
}

class EventObjectsTable : Table("event_object") {
    val ocelEventId = text("ocel_event_id")
    val ocelObjectId = text("ocel_object_id")
    val ocelObjectQualifier = text("ocel_qualifier")
}

class ObjectsTable : Table("object") {
    val ocelObjectId = text("ocel_id")
    val ocelObjectType = text("ocel_type")
}

class ObjectObjectsTable : Table("object_object") {
    val ocelSourceObjectId = text("ocel_source_id")
    val ocelTargetObjectId = text("ocel_target_id")
    val ocelObjectQualifier = text("ocel_qualifier")
}

class ConcreteEventTable(tableName: String) : Table(name = tableName) {
    val ocelEventId = text("ocel_id")
    val ocelTime = text("ocel_time")

    //  can be more attributes here like  val totalItems = text("total_items")
    companion object {
        fun createTableName(eventLabel: String): String {
            val camelCase = eventLabel.split(" ").map { it.replaceFirstChar { it.uppercase() } }.joinToString("")
            return "event_$camelCase"
        }

        fun convertTypeToTypeMap(ocelType: String): String {
            return ocelType.split(" ").joinToString("")
        }
    }
}

class EventTypeMapTable : Table("event_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")
}

class ObjectTypeMapTable : Table("object_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")
}

class ConcreteObjectTable(tableName: String) : Table(name = tableName) {
    val ocelObjectId = text("ocel_id")
    val ocelTime = text("ocel_time") // unix timestamp 1970-01-01 00:00:00 ISO 8601
    val ocelChangedField = text("ocel_changed_field").nullable()
    // can be more attributes here
}