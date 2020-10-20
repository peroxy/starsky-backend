package starsky.api.database.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.jetbrains.exposed.sql.Database

@Configuration
class DbConfig {
    @Autowired
    lateinit var dataSource: DataSource

    /**
     * Register the [Database] instance as a Spring bean.
     */
    @Bean
    fun database(): Database {
        return Database.connect(dataSource)
    }

}