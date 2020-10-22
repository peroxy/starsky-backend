package com.starsky.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DbConfig {
    private val password: String = System.getenv("POSTGRES_PASSWORD")
        ?: throw java.lang.IllegalStateException("POSTGRES_PASSWORD environment variable is missing!")

    var connection : DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/starsky"
        config.username = "starsky"
        config.password = password
        //config.driverClassName = "com.mysql.jdbc.Driver"
        return HikariDataSource(config)
    }
}