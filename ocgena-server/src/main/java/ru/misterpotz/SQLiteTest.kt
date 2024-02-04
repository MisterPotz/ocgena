package ru.misterpotz

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.sql.Connection


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