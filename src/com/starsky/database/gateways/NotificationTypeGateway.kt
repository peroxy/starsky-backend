package com.starsky.database.gateways

import com.starsky.models.NotificationType
import com.starsky.models.NotificationTypes

object NotificationTypeGateway : Gateway() {
    fun getByName(name: String): NotificationType? {
        var notificationType: NotificationType? = null
        transaction {
            notificationType = NotificationType.find { NotificationTypes.name eq name }.limit(1).firstOrNull()
        }
        return notificationType
    }
}