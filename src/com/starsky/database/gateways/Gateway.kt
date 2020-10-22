package com.starsky.database.gateways

import com.starsky.database.DbConfig
import org.jetbrains.exposed.sql.Database

abstract class Gateway {
    protected fun connect() : Database {
        return Database.connect(DbConfig.connection)
    }
}