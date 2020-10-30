package com.starsky.database

import com.starsky.env.Environment
import com.starsky.env.EnvironmentVars
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DbConfig {
    var connection : DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()

        when(EnvironmentVars.environment){
            Environment.DEV -> {
                config.jdbcUrl = "jdbc:postgresql://localhost:5432/starsky"
                config.username = "starsky"
                config.password = EnvironmentVars.postgresPassword
            }
            Environment.PROD -> config.jdbcUrl = EnvironmentVars.connectionString
            else -> throw NotImplementedError()
        }

        return HikariDataSource(config)
    }
}