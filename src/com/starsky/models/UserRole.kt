package com.starsky.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


object UserRoles : IntIdTable("user_role") {
    val name = text("name")
}


class UserRole(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserRole>(UserRoles)

    var name by UserRoles.name
}

enum class UserRoleEnum(val id: Int) {
    Manager(1),
    Employee(2),
    Admin(3);

    companion object {
        fun valueOf(value: Int): UserRoleEnum? = values().find { it.id == value }
    }
}