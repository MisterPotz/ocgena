package ru.misterpotz.convert

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


object EventsTable : Table("event") {
    val ocelEventId = text("ocel_id").references(EventTypeMapTable.ocelType)
    val ocelEventType = text("ocel_type")

    override val primaryKey = PrimaryKey(ocelEventId)
}

object EventObjectsTable : Table("event_object") {
    val ocelEventId = text("ocel_event_id").references(EventsTable.ocelEventId)
    val ocelObjectId = text("ocel_object_id").references(ObjectsTable.ocelObjectId)
    val ocelObjectQualifier = text("ocel_qualifier").default("")

    override val primaryKey = PrimaryKey(ocelEventId, ocelObjectId, ocelObjectQualifier)
}

object ObjectsTable : Table("object") {
    val ocelObjectId = text("ocel_id")
    val ocelObjectType = text("ocel_type").references(ObjectTypeMapTable.ocelType)

    override val primaryKey = PrimaryKey(ocelObjectId)
}

object ObjectObjectsTable : Table("object_object") {
    val ocelSourceObjectId = text("ocel_source_id").references(ObjectsTable.ocelObjectId)
    val ocelTargetObjectId = text("ocel_target_id").references(ObjectsTable.ocelObjectId)
    val ocelObjectQualifier = text("ocel_qualifier").default("")

    override val primaryKey = PrimaryKey(ocelSourceObjectId, ocelTargetObjectId, ocelObjectQualifier)
}

class ConcreteEventTable(tableName: String) : Table(name = tableName) {
    val ocelEventId = text("ocel_id")
    val ocelTime = timestamp("ocel_time")

    override val primaryKey = PrimaryKey(ocelEventId)
    //  can be more attributes here like  val totalItems = text("total_items")
    companion object {
        fun createTableName(eventMapType: String): String {
            return "event_$eventMapType"
        }
    }
}

object EventTypeMapTable : Table("event_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")

    override val primaryKey = PrimaryKey(ocelType)
}

object ObjectTypeMapTable : Table("object_map_type") {
    val ocelType = text("ocel_type")
    val ocelTypeMap = text("ocel_type_map")

    override val primaryKey = PrimaryKey(ocelType)
}

class ConcreteObjectTable(tableName: String) : Table(name = tableName) {
    val ocelObjectId = text("ocel_id").references(ObjectsTable.ocelObjectId)
    val ocelTime = timestamp("ocel_time") // unix timestamp 1970-01-01 00:00:00 ISO 8601
    val ocelChangedField = text("ocel_changed_field").default("")
    // can be more attributes here

    companion object {
        fun createTableName(eventMapType: String): String {
            return "object_$eventMapType"
        }
    }
}