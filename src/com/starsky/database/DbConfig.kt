package com.starsky.database

import com.starsky.api.Environment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DbConfig {

    private val environment: Environment = Environment.values().firstOrNull { it.name == System.getenv("STARSKY_ENVIRONMENT") } ?: Environment.DEV
    private val connectionString: String = System.getenv("JDBC_DATABASE_URL")
        ?: throw java.lang.IllegalStateException("JDBC_DATABASE_URL environment variable is missing!")
//    private val password: String = System.getenv("POSTGRES_PASSWORD")
//        ?: throw java.lang.IllegalStateException("POSTGRES_PASSWORD environment variable is missing!")

    var connection : DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = connectionString

        //TODO: IF ENV == DEV, use localhost docker:
        //config.jdbcUrl = "jdbc:postgresql://localhost:5432/starsky"
        //config.username = "starsky"
        //config.password = password
        return HikariDataSource(config)
    }
}