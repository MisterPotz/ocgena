package ru.misterpotz.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.pathString


interface DBConnectionSetupper {
    fun createConnection(path: Path): Connection
    class Connection(val hikariDataSource: HikariDataSource, val database: Database)
}

class DBConnectionSetupperImpl @Inject constructor() : DBConnectionSetupper {
    override fun createConnection(path: Path): DBConnectionSetupper.Connection {
        val hikariSqlConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:${path.pathString}"
            driverClassName = "org.sqlite.JDBC"
        }
        val hikariDataSource = HikariDataSource(hikariSqlConfig)
        val connect = Database.connect(hikariDataSource)
        TransactionManager.manager.defaultIsolationLevel =
            java.sql.Connection.TRANSACTION_SERIALIZABLE

        return DBConnectionSetupper.Connection(hikariDataSource, connect)
    }
}