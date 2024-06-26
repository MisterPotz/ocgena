package ru.misterpotz.simulation

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.statements.BatchInsertStatement


const val STEP_NUMBER = "stepNumber"

class StepToMarkingAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)
class StepToFiringAmountsTable(columnNames: List<String>) : StepToVariableColumnsIntTable(columnNames)

//class StepToMarkingTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
class StepToFiringTokensTable(columnNames: List<String>) : StepToVariableColumnsTextTable(columnNames)
object TokensTable : Table() {
    val tokenId = long("id")
    val objectTypeId = reference("objectTypeId", ObjectTypeTable.objectTypeId)
    override val primaryKey = PrimaryKey(tokenId)
}

object ObjectTypeTable : Table() {
    val objectTypeId = text("objectTypeId")
    val objectTypeLabel = text("label")
    override val primaryKey = PrimaryKey(objectTypeId)
}

object SimulationStepsTable : LongIdTable(columnName = "stepNumber") {
    val clockIncrement = long("clockIncrement")
    val totalClock = long("totalClock")
    val chosenTransition = text("chosenTransition").nullable().default(null)
    val transitionDuration = long("transitionDuration").nullable()
}

object PlacesTable : Table() {
    val placeId = text("placeId")
    override val primaryKey: PrimaryKey = PrimaryKey(placeId)
}

object TransitionToLabelTable : Table() {
    val transitionId = text("transitionId")
    val transitionLabel = text("label")

    override val primaryKey = PrimaryKey(transitionId)
}

abstract class StepToVariableColumnsIntTable(private val columnNames: List<String>) : Table() {
    val stepNumber = reference(STEP_NUMBER, SimulationStepsTable)
    override val primaryKey = PrimaryKey(stepNumber)

    val integerColumns = mutableListOf<Column<Int>>()

    init {
        for (i in columnNames) {
            val column = integer(i).default(0)
            integerColumns.add(column)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun column(placeName: String): Column<Int> {
        return integerColumns.find { it.name == placeName }!!
    }
}

fun ResultRow.getIntMap(stepToVariableColumnsIntTable: StepToVariableColumnsIntTable): Map<String, Int>? {
    return if (stepToVariableColumnsIntTable.exists()) {
        buildMap {
            for (column in stepToVariableColumnsIntTable.integerColumns) {
                put(column.name, this@getIntMap[column])
            }
        }
    } else {
        null
    }
}


fun ResultRow.getStringMap(stepToVariableColumnsTextTable: StepToVariableColumnsTextTable): Map<String, String?>? {
    return if (stepToVariableColumnsTextTable.exists()) {
        buildMap {
            for (column in stepToVariableColumnsTextTable.textColumns) {
                put(column.name, this@getStringMap[column])
            }
        }
    } else {
        null
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
    valueTransformer: (List<Long>) -> String
) {
    for ((key, value) in map) {
        this[table.column(keyNameTransformer(key))] = valueTransformer(value)
    }
}

abstract class StepToVariableColumnsTextTable(private val columnNames: List<String>) : Table() {
    val stepNumber = reference(STEP_NUMBER, SimulationStepsTable)
    override val primaryKey = PrimaryKey(stepNumber)

    val textColumns = mutableListOf<Column<String?>>()

    init {
        for (i in columnNames) {
            textColumns.add(text(i).nullable().default(defaultValue = null))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun column(placeName: String): Column<String?> {
        return textColumns.find { it.name == placeName }!!
    }
}