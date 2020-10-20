package starsky.api

import com.google.gson.Gson
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import starsky.api.database.models.User
import starsky.api.database.repositories.UserRepository

@RestController
class UserController {
//    @Autowired
//    lateinit var database: Database

    private val gson = Gson()

    @GetMapping("/api/user/{id}")
    fun getUser(@PathVariable id: Int): ResponseEntity<String> {
        val user = UserRepository().getUserById(id)

        if (user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID=$id does not exist.")
        }
        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(user))
    }

    @GetMapping("/user")
    fun getUsers(): String {
        return "ITS WORKING MY DUDE!!!!"
    }

}