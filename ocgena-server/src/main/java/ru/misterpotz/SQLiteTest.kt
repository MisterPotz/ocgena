package ru.misterpotz

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


object Users : Table() {
    val id: Column<String> = varchar("id", 10)
    val name: Column<String> = varchar("name", length = 50)
    val cityId: Column<Int?> = (integer("city_id") references Cities.id).nullable()

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID") // name is optional here
}

object Cities : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 50)

    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
}
