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

fun main() {
    val serverSimulationComponent = ServerSimulationComponent.create(
        ServerSimulationConfig(
            Path("data", "data.db"),
            simulationConfig = buildConfig {
                ocNetType = OcNetType.LOMAZOVA
                ocNetStruct = buildOCNet {
                    buildingBlockTwoInTwoOutMiddle().installOnto(this)
                }
                semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
            }.withInitialMarking {
                put("p1", 10)
            }
        )
    )
    val domainComponent = DomainComponent.create(serverSimulationComponent)
    val simulationComponent =
        SimulationComponent.defaultCreate(serverSimulationComponent.simulationConfig(), domainComponent)

    runBlocking {
        simulationComponent.simulationTask().prepareAndRunAll()
    }
}

fun main3() {
    val serverSimulationComponent = ServerSimulationComponent.create(
        ServerSimulationConfig(
            Path("data", "data.db"),
            simulationConfig = buildConfig {
                ocNetType = OcNetType.LOMAZOVA
                ocNetStruct = buildOCNet {
                    buildingBlockTwoInTwoOutMiddle().installOnto(this)
                }
                semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
            }
        )
    )
    val domainComponent = DomainComponent.create(serverSimulationComponent)
    val simulationComponent =
        SimulationComponent.defaultCreate(serverSimulationComponent.simulationConfig(), domainComponent)
    val repository = serverSimulationComponent.simulationLogRepository()

    runBlocking {
        repository.pushInitialData()
    }
}

fun main2() {

//    val sqliteDataSource = SQLiteDataSource(SQLiteConfig()).apply {
//        url = "jdbc:sqlite:data/data.db"
//    }
    val hikariSqlConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:data/data.db"
        driverClassName = "org.sqlite.JDBC"
    }
    val dataSource = HikariDataSource(hikariSqlConfig)

//    sqliteDataSource.connection.close()
    val db = Database.connect(dataSource)
    db.transactionManager.newTransaction().close()
//    val db = Database.connect("jdbc:sqlite:data/data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE

//    val autoTable = CoolioTable(listOf("p1", "p2", "o1", "o2"))

    val stepToMarkingAmounts = StepToMarkingAmountsTable(listOf("p1", "p2", "o1", "o2"))
//    db.close()

    transaction(db) {
        // print sql to std-out

        SchemaUtils.create(Cities)
        SchemaUtils.create(SimulationStepsTable)
        SchemaUtils.create(stepToMarkingAmounts)

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        val stPeteId = Cities.insert {
            it[name] = "St. Petersburg"
        } get Cities.id

        val stepNumber = SimulationStepsTable.insert {
            it[clockIncrement] = 10
            it[chosenTransition] = "p1"
        } get SimulationStepsTable.id

        val stepToMarkingAmount = stepToMarkingAmounts.insert {
            it[column("p1")] = 4
            it[stepToMarkingAmounts.stepNumber] = stepNumber
        } get stepToMarkingAmounts.stepNumber

        println("stpete id $stPeteId")
        // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
        println("Cities: ${Cities.selectAll()}")
    }
    dataSource.close()
}