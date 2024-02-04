package ru.misterpotz

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.BatchInsertStatement


const val STEP_NUMBER = "stepNumber"

class StepToMarkingAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)
class StepToFiringAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)

//class StepToMarkingTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
class StepToFiringTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
object TokensTable : LongIdTable() {
    val objectTypeId = reference("objectTypeId", ObjectTypeTable.objectTypeId)
}

object ObjectTypeTable : Table() {
    val objectTypeId = varchar("objectTypeId", length = 10)
    override val primaryKey = PrimaryKey(objectTypeId)
}

object SimulationStepsTable : LongIdTable(columnName = "stepNumber") {
    val clockIncrement = long("clockIncrement")
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

fun BatchInsertStatement.insertBatchMap(
    table: StepToVariableColumnsIntTable,
    map: Map<String, Int>,
    keyNameTransformer: ((String) -> String) = { it }
) {
    for ((key, value) in map) {
        this[table.column(keyNameTransformer(key))] = value
    }
}


fun BatchInsertStatement.insertBatchMapTransform(
    table: StepToVariableColumnsTextTable,
    map: Map<String, List<Long>>,
    keyNameTransformer: ((String) -> String),
    valueTransformer : (List<Long>) -> String
) {
    for ((key, value) in map) {
        this[table.column(keyNameTransformer(key))] = valueTransformer(value)
    }
}

abstract class StepToVariableColumnsTextTable(private val columnNames: List<String>) : Table() {
    val stepNumber = reference(STEP_NUMBER, SimulationStepsTable)
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