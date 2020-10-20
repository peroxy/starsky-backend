package starsky.api.database.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.format.ISODateTimeFormat

data class UserDto(val name: String, val email: String, val password: String, val dateCreated: String)

object Users : IntIdTable("user") {
    val name = text("name")
    val email = text("email")
    val password = text("password")
    val dateCreated = datetime("date_created")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var name by Users.name
    var email by Users.email
    var password by Users.password
    var dateCreated by Users.dateCreated

    fun toModel():UserDto{
        return UserDto(name, email, password, ISODateTimeFormat.dateTimeNoMillis().print(dateCreated))
    }
}
