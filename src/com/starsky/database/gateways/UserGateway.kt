package com.starsky.database.gateways

import com.starsky.database.DbConfig
import com.starsky.models.User
import com.starsky.models.UserDto
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class UserGateway : Gateway() {

    fun getById(id: Int): UserDto? {
        var user : User? = null
        transaction(connect()) {
            addLogger(StdOutSqlLogger)
            user = User.findById(id)
        }

        return user?.toModel()
    }
}