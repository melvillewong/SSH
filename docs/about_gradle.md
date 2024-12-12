## Future development
+ Detailed description here
+ Implement test

## Run Server and Client
1. Ensure Docker Desktop is running.
2. `./gradlew clean build` to delete all builded files and rebuild all from clean.
3. `./gradlew :app:server:run` to create container, and run the server.
4. Open a new terminal, `java -jar app/client/build/libs/client-1.0.0.jar` to run client.

client-side
```
❯ java -jar app/client/build/libs/client-1.0.0.jar
Enter Resident's First Name: John
Enter Resident's Last Name: Cena
Connecting to 127.0.0.1 on port 9994
Client: Requesting records database service for user command
John;Cena
Client: Processing CachedRowSet...
Resident ID: 0 | Start Time: 2024-12-03 15:29:36.0 | End Time: 2024-12-03 17:15:12.0 | Status: Empty
Resident ID: 0 | Start Time: 2024-12-06 07:43:47.0 | End Time: 2024-12-06 08:52:15.0 | Status: Empty
```

## Key files
+ `app/` to hold all modules (server, service, client)
+ `build.gradle.kts` 
    - Gradle build script.
    - Located in the root and all three modules, 4 in total.
    - To configure how Gradle builds and manages your project.
    - To specifies which dependencies and tasks should be executed during the build process.
+ `gradlew`, `gradlew.bat` 
    - Gradle Wrapper scripts
    - Ensures consistent Gradle versions across all environments (e.g., different team members or CI/CD environments).
    - You run commands with `./gradlew` (Unix-based) or `gradlew.bat` (Windows-based) instead of gradle.
+ `settings.gradles.kts`
    - Defines the configuration for the Gradle multi-project build.
    - Used to specifying the root project and any included subprojects.
    - include() all your submodules.

## Access to Docker PostgreSQL Container
I have added **docker plugin** and several **Docker-related tasks** in the server module (configured in `app/server/build.gradle.kts`)
+ Ensure Docker Desktop is running.
+ It should automatically create the Docker container with docker-compose when running the server with `./gradlew :app:server:run`.

Other useful Docker-related Gradle's tasks:
+ `./gradlew createPostgresContainer`
+ `./gradlew destroyPostgresContainer`
+ `./gradlew startPostgresContainer`
+ `./gradlew stopPostgresContainer`
+ `./gradlew restartPostgresContainer`
+ `./gradlew checkMigrationPostgresContainer`