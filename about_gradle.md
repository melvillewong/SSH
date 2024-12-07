## Future development
+ Script for Server.java, Service.java, Client.java (Sample currently)
+ Detailed description here
+ Implement test

## Run Server and Client (Sample)
1. `./gradlew :app:server:run` to run server
2. Open a new terminal, `./gradlew :app:client:run` to run client

Server-side
```
❯ ./gradlew :app:server:run
Reusing configuration cache.

> Task :app:server:run
Server is listening on port 12345...
New client connected
Received from client: Hello, Server!
<===========--> 87% EXECUTING [24s]
> :app:server:run
```
Client-side
```
❯ ./gradlew :app:client:run
Starting a Gradle Daemon, 1 busy and 2 incompatible and 3 stopped Daemons could not be reused, use --status for details
Reusing configuration cache.

> Task :app:client:run
Response from server: Service processed message: !revreS ,olleH

BUILD SUCCESSFUL in 4s
2 actionable tasks: 2 executed
Configuration cache entry reused.
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