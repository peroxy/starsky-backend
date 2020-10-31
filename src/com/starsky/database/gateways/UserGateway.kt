package com.starsky.database.gateways

import com.starsky.models.User
import com.starsky.models.Users
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.mindrot.jbcrypt.BCrypt

object UserGateway : Gateway() {

    fun getById(id: Int): User? {
        var user : User? = null
        transaction {
            user = User.findById(id)
        }
        return user
    }

    fun getByEmail(email: String): User? {
        var user: User? = null
        transaction {
            user = User.find { Users.email eq email }.limit(1).firstOrNull()
        }
        return user
    }

    fun insert(email: String, name: String, password: String): User {
        var user: User? = null
        transaction {
            user = User.new {
                this.name = name
                this.email = email
                this.password = BCrypt.hashpw(password, BCrypt.gensalt())
                dateCreated = DateTime.now(DateTimeZone.UTC)
            }
        }
        return user!!
    }

}