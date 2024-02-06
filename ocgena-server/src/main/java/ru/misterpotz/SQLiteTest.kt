package ru.misterpotz

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import ru.misterpotz.di.ServerSimulationComponent
import ru.misterpotz.di.ServerSimulationConfig
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.buildConfig
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.testing.buildingBlockTwoInTwoOutMiddle
import ru.misterpotz.ocgena.testing.installOnto
import java.sql.Connection
import kotlin.io.path.Path


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

