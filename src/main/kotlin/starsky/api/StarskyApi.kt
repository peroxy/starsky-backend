package starsky.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StarskyApi

fun main(args: Array<String>) {
    runApplication<StarskyApi>(*args)
}
