package com.starsky.database.gateways

import com.starsky.database.DbConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Gateway {
    private fun connect(): Database {
        return Database.connect(DbConfig.connection)
    }

    /**
     * Connect and log the sql transaction provided by [execute] lambda
     */
    protected fun <T> transaction(execute: Transaction.() -> T): T {
        return transaction(connect()) {
            addLogger(StdOutSqlLogger)
            execute()
        }
    }

}