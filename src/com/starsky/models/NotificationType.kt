package com.starsky.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object NotificationTypes : IntIdTable("notification_type") {
    val name = text("name")
}


class NotificationType(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NotificationType>(NotificationTypes)

    var name by NotificationTypes.name
}

enum class NotificationTypeEnum(val id: Int) {
    Email(1),
    TextMessage(2);

    companion object {
        fun valueOf(value: Int): NotificationTypeEnum? = NotificationTypeEnum.values().find { it.id == value }
    }
}