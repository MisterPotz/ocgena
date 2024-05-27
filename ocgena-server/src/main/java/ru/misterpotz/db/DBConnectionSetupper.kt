package ru.misterpotz.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Path
import kotlin.io.path.pathString

object DBConnectionSetupper {
    fun createConnection(path: Path): Connection {
        val hikariSqlConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${path.pathString}"
            driverClassName = "org.sqlite.JDBC"
        }
        val hikariDataSource = HikariDataSource(hikariSqlConfig)
        val connect = Database.connect(hikariDataSource)
        TransactionManager.manager.defaultIsolationLevel =
            java.sql.Connection.TRANSACTION_SERIALIZABLE
        return Connection(hikariDataSource, connect)
    }

    class Connection(private val hikariDataSource: HikariDataSource, val database: Database) {
        fun close() {
            hikariDataSource.close()
        }
    }
}