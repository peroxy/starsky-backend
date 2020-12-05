package com.starsky.models

import com.starsky.api.responses.UserResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.format.ISODateTimeFormat

object Users : IntIdTable("user") {
    val name = text("name")
    val email = text("email")
    val password = text("password")
    val jobTitle = text("job_title")
    val phoneNumber = text("phone_number").nullable()
    val notificationTypeId = integer("notification_type_id")
    val userRoleId = integer("user_role_id")
    val parentUserId = integer("parent_user_id").nullable()
    val dateCreated = datetime("date_created")
    val enabled = bool("enabled")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var jobTitle by Users.jobTitle
    var phoneNumber by Users.phoneNumber
    var notificationTypeId by Users.notificationTypeId
    var userRoleId by Users.userRoleId
    var parentUserId by Users.parentUserId
    var enabled by Users.enabled
    var dateCreated by Users.dateCreated

    fun toResponse(): UserResponse {
        return UserResponse(
            id.value,
            name,
            email,
            jobTitle,
            phoneNumber,
            NotificationTypeEnum.valueOf(notificationTypeId).toString().toLowerCase(),
            UserRoleEnum.valueOf(userRoleId).toString().toLowerCase(),
            ISODateTimeFormat.dateTimeNoMillis().print(dateCreated)
        )
    }
}





