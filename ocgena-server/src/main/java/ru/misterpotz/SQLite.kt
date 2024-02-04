package ru.misterpotz

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.sql.Connection

const val STEP_NUMBER = "stepNumber"

class StepToMarkingAmounts(columnNames: List<String>) : StepToVariableColumnsInt(columnNames)
class StepToFiringAmounts(columnNames: List<String>) : StepToVariableColumnsInt(columnNames)
class StepToMarkingTokens(columnNames: List<String>) : StepToVariableColumnsText(columnNames)
class StepToFiringTokens(columnNames: List<String>) : StepToVariableColumnsText(columnNames)
object Tokens : LongIdTable() {
    val tokenType = reference("tokenTypeId", ObjectType)
}

object ObjectType : LongIdTable(columnName = "tokenTypeId")

object SimulationSteps : LongIdTable(columnName = "stepNumber") {
    val clockIncrement = integer("clockIncrement")
    val chosenTransition = varchar("chosenTransition", 10).nullable().default(null)
}
//object StepToMarkingTokens : Table() {
//    val
//}

abstract class StepToVariableColumnsInt(private val columnNames: List<String>) : Table() {
    val stepNumber = reference(STEP_NUMBER, SimulationSteps)
    override val primaryKey = PrimaryKey(stepNumber)

    init {
        for (i in columnNames) {
            integer(i).default(0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun column(placeName: String): Column<Int> {
        return columns.find { it.name == placeName }!! as Column<Int>
    }
}

abstract class StepToVariableColumnsText(private val columnNames: List<String>) : Table() {
    private val stepNumber = reference(STEP_NUMBER, SimulationSteps)
    override val primaryKey = PrimaryKey(stepNumber)

    init {
        for (i in columnNames) {
            text(i).nullable().default(defaultValue = null)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun column(placeName: String): Column<String> {
        return columns.find { it.name == placeName }!! as Column<String>
    }
}


abstract class StepToVariableColumnsBlob(private val columnNames: List<String>) :
    LongIdTable(columnName = STEP_NUMBER) {
    init {

    }
}

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

    val sqliteDataSource = SQLiteDataSource(SQLiteConfig()).apply {
        url = "jdbc:sqlite:data/data.db"
    }

    sqliteDataSource.connection.close()
    val db = Database.connect(sqliteDataSource)
    db.transactionManager.newTransaction().close()
//    val db = Database.connect("jdbc:sqlite:data/data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE

//    val autoTable = CoolioTable(listOf("p1", "p2", "o1", "o2"))

    val stepToMarkingAmounts = StepToMarkingAmounts(listOf("p1", "p2", "o1", "o2"))
//    db.close()

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(Cities)
        SchemaUtils.create(SimulationSteps)
        SchemaUtils.create(stepToMarkingAmounts)

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        val stPeteId = Cities.insert {
            it[name] = "St. Petersburg"
        } get Cities.id

        val stepNumber = SimulationSteps.insert {
            it[clockIncrement] = 10
            it[chosenTransition] = "p1"
        } get SimulationSteps.id

        val stepToMarkingAmount = stepToMarkingAmounts.insert {
            it[column("p1")] = 4
            it[stepToMarkingAmounts.stepNumber] = stepNumber
        } get stepToMarkingAmounts.stepNumber

        println("stpete id $stPeteId")
        // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
        println("Cities: ${Cities.selectAll()}")
    }

}