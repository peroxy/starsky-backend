package com.starsky.database.gateways

import com.starsky.database.DbConfig
import com.starsky.models.User
import com.starsky.models.Users
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object UserGateway : Gateway() {

    fun getById(id: Int): User? {
        var user : User? = null
        transaction(connect()) {
            addLogger(StdOutSqlLogger)
            user = User.findById(id)
        }

        return user
    }

    fun getByEmail(email: String): User? {
        var user : User? = null
        transaction(connect()) {
            addLogger(StdOutSqlLogger)
            user = User.find { Users.email eq email }.limit(1).firstOrNull()
        }

        return user
    }
}