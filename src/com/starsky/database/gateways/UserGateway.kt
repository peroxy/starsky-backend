package com.starsky.database.gateways

import com.starsky.models.User
import com.starsky.models.Users
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object UserGateway : Gateway() {

    fun getById(id: Int): User? {
        var user: User? = null
        transaction {
            user = User.find { Users.id eq id and Users.enabled }.limit(1).firstOrNull()

        }
        return user
    }

    fun getByEmail(email: String): User? {
        var user: User? = null
        transaction {
            user = User.find { Users.email eq email and Users.enabled }.limit(1).firstOrNull()
        }
        return user
    }

    fun insert(
        email: String,
        name: String,
        hashedPassword: String,
        jobTitle: String,
        notificationTypeId: Int,
        phoneNumber: String?,
        userRoleId: Int,
        parentUserId: Int?
    ): User {
        var user: User? = null
        transaction {
            user = User.new {
                this.name = name
                this.email = email
                this.password = hashedPassword
                this.dateCreated = DateTime.now(DateTimeZone.UTC)
                this.enabled = true
                this.jobTitle = jobTitle
                this.notificationTypeId = notificationTypeId
                this.phoneNumber = phoneNumber
                this.userRoleId = userRoleId
                this.parentUserId = parentUserId
            }
        }
        return user!!
    }

}

