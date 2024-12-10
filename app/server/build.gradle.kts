plugins {
    id("com.bmuschko.docker-remote-api") version "7.1.0"                // docker plugin
}

application {
    mainClass.set("com.example.server.Server")
}

dependencies {
    implementation(project(":app:service"))
    // implementation("org.postgresql:postgresql:42.5.0")                  // PostgreSQL JDBC driver
}

// Docker
val composeFile = "../../docker-compose.yml"

docker {
    url.set("http://localhost:2375")
}

tasks.register("createPostgresContainer") {
    doLast {
        exec {
            commandLine("docker-compose", "-f", composeFile, "up", "-d")
        }
    }
    notCompatibleWithConfigurationCache("Exec tasks using external processes cannot be cached.")
}

tasks.register("destroyPostgresContainer") {
    doLast {
        exec {
            commandLine("docker-compose", "-f", composeFile, "down")
        }
    }
    notCompatibleWithConfigurationCache("Exec tasks using external processes cannot be cached.")
}

tasks.register("startPostgresContainer") {
    doLast {
        exec {
            commandLine("docker-compose", "-f", composeFile, "start")
        }
    }
    notCompatibleWithConfigurationCache("Exec tasks using external processes cannot be cached.")
}

tasks.register("stopPostgresContainer") {
    doLast {
        exec {
            commandLine("docker-compose", "-f", composeFile, "stop")
        }
    }
    notCompatibleWithConfigurationCache("Exec tasks using external processes cannot be cached.")
}

tasks.register("restartPostgresContainer") {
    doLast {
        exec {
            commandLine("docker-compose", "-f", composeFile, "restart")
        }
    }
    notCompatibleWithConfigurationCache("Depends on tasks incompatible with configuration cache.")
}

// Check if accessing Postgres container database successfully, retry 10 times 
tasks.register("waitForPostgres") {
    dependsOn("createPostgresContainer")

    doLast {
        val maxAttempts = 10
        val waitTime = 2000L // 2 seconds
        var attempt = 0
        var isReady = false

        val targetDatabase = "ssh_smart_scheduling"

        while (attempt < maxAttempts && !isReady) {
            try {
                exec {
                    commandLine(
                        "docker", "exec", "postgres_server",
                        "psql", "-U", "postgres", "-d", targetDatabase
                    )
                }
                isReady = true
            } catch (e: Exception) {
                attempt++
                if (attempt < maxAttempts) {
                    println("Postgres is not ready, retrying in $waitTime ms...")
                    Thread.sleep(waitTime)
                } else {
                    println("Max attempts reached, unable to connect to Postgres.")
                }
            }
        }

        if (!isReady) {
            throw GradleException("PostgreSQL is not ready after $maxAttempts attempts.")
        }
    }
}

// run "waitForPostgres" before running server
tasks.named<JavaExec>("run") {
    dependsOn("waitForPostgres")
}

// Check if migration is done
tasks.register("checkMigrationPostgresContainer") {
    doLast {
        val containerName = "postgres_server"  // Your container name
        val username = "postgres"              // PostgreSQL username (usually postgres)
        val database = "ssh_smart_scheduling"         // Target database name

        // Run the docker exec command to access PostgreSQL
        exec {
            commandLine(
                "docker", "exec", containerName, 
                "psql", "-U", username, 
                "-d", database, 
                "-c", "SELECT * FROM flyway_schema_history;"
            )
        }
    }
    notCompatibleWithConfigurationCache("Exec tasks using external processes cannot be cached.")
}