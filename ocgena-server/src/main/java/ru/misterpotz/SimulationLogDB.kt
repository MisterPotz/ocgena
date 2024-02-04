package ru.misterpotz

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


const val STEP_NUMBER = "stepNumber"

class StepToMarkingAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)
class StepToFiringAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)
class StepToMarkingTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
class StepToFiringTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
object TokensTable : LongIdTable() {
    val tokenType = reference("tokenTypeId", ObjectTypeTable)
}

object ObjectTypeTable : LongIdTable(columnName = "tokenTypeId")

object SimulationStepsTable : LongIdTable(columnName = "stepNumber") {
    val clockIncrement = integer("clockIncrement")
    val chosenTransition = varchar("chosenTransition", 10).nullable().default(null)
}

abstract class StepToVariableColumnsIntTable(private val columnNames: List<String>) : Table() {
    val stepNumber = reference(STEP_NUMBER, SimulationStepsTable)
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

abstract class StepToVariableColumnsTextTable(private val columnNames: List<String>) : Table() {
    private val stepNumber = reference(STEP_NUMBER, SimulationStepsTable)
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