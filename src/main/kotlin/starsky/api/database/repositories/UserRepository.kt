package starsky.api.database.repositories

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import starsky.api.database.models.User
import starsky.api.database.models.UserDto

class UserRepository {
    @Autowired
    lateinit var database: Database

    fun getUserById(id: Int): UserDto? {
        var user : User? = null
        transaction {
            addLogger(StdOutSqlLogger)
            user = User.findById(id)
        }

        return user?.toModel()
    }
}