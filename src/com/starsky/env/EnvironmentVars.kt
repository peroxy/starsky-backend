package com.starsky.env

object EnvironmentVars {

    /**
     * Backend application's environment. Will default to Environment.DEV if no env variable is found.
     */
    val environment: Environment =
        Environment.values().firstOrNull { it.name == System.getenv("STARSKY_ENVIRONMENT") } ?: Environment.DEV

    /**
     * Starsky front-end hostname used for CORS Origin host access, e.g. api.domain.com (defaults to localhost:3000 for DEV environment).
     */
    val frontendHost: String
        get() {
            return when (environment) {
                Environment.DEV -> System.getenv("STARSKY_FRONTEND_HOST") ?: "localhost:3000"
                Environment.PROD -> System.getenv("STARSKY_FRONTEND_HOST")
                    ?: throw java.lang.IllegalStateException("STARSKY_FRONTEND_HOST environment variable is missing, CORS will not work!")
                else -> throw NotImplementedError() // in case we add a new environment we won't forget this way
            }
        }


    /**
     * Connection string with username and password appended.
     * This is always present in Heroku container and is only used if environment is production.
     **/
    val connectionString: String
        get() {
            return when (environment) {
                Environment.PROD -> System.getenv("JDBC_DATABASE_URL")
                    ?: throw java.lang.IllegalStateException("JDBC_DATABASE_URL environment variable is missing!")
                else -> throw NotImplementedError("JDBC_DATABASE_URL is not supported in non-production environment!")
            }
        }

    /**
     * Postgres password that is used to connect to local development docker database.
     * This is only used for development purposes and is not used in production.
     **/
    val postgresPassword: String
        get() {
            return when (environment) {
                Environment.DEV -> System.getenv("POSTGRES_PASSWORD")
                    ?: throw java.lang.IllegalStateException("POSTGRES_PASSWORD environment variable is missing!")
                else -> throw NotImplementedError("POSTGRES_PASSWORD is not supported in production environment!")
            }
        }

    /**
     * JWT secret value used for generating authentication bearer tokens.
     **/
    val jwtSecret: String = System.getenv("STARSKY_JWT_SECRET")
        ?: throw java.lang.IllegalStateException("STARSKY_JWT_SECRET environment variable is missing, API authentication will not work!")
}