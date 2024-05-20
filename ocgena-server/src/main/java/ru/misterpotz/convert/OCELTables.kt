package ru.misterpotz.convert

import org.jetbrains.exposed.sql.Table


class EventsTable : Table("event") {
    val ocelEventId = text("ocel_id")
    val ocelEventType = text("ocel_type")

    override val primaryKey = PrimaryKey(ocelEventId)
}

class EventObjectsTable : Table("event_object") {
    val ocelEventId = text("ocel_event_id")
    val ocelObjectId = text("ocel_object_id")
    val ocelObjectQualifier = text("ocel_qualifier").default("")

    override val primaryKey = PrimaryKey(ocelEventId, ocelObjectId, ocelObjectQualifier)
}

class ObjectsTable : Table("object") {
    val ocelObjectId = text("ocel_id")
    val ocelObjectType = text("ocel_type")

    override val primaryKey = PrimaryKey(ocelObjectId)
}

class ObjectObjectsTable : Table("object_object") {
    val ocelSourceObjectId = text("ocel_source_id")
    val ocelTargetObjectId = text("ocel_target_id")
    val ocelObjectQualifier = text("ocel_qualifier").default("")

    override val primaryKey = PrimaryKey(ocelSourceObjectId, ocelTargetObjectId, ocelObjectQualifier)
}

class ConcreteEventTable(tableName: String) : Table(name = tableName) {
    val id = long("id").autoIncrement()
    val ocelEventId = text("ocel_id")
    val ocelTime = text("ocel_time")

    override val primaryKey = PrimaryKey(id)
    //  can be more attributes here like  val totalItems = text("total_items")
    companion object {
        fun createTableName(eventMapType: String): String {
            return "event_$eventMapType"
        }
    }
}

class EventTypeMapTable : Table("event_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")

    override val primaryKey = PrimaryKey(ocelType)
}

class ObjectTypeMapTable : Table("object_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")

    override val primaryKey = PrimaryKey(ocelType)
}

class ConcreteObjectTable(tableName: String) : Table(name = tableName) {
    val id = long("id").autoIncrement()
    val ocelObjectId = text("ocel_id")
    val ocelTime = text("ocel_time") // unix timestamp 1970-01-01 00:00:00 ISO 8601
    val ocelChangedField = text("ocel_changed_field").default("")
    // can be more attributes here

    override val primaryKey = PrimaryKey(id)

    companion object {
        fun createTableName(eventMapType: String): String {
            return "object_$eventMapType"
        }
    }
}