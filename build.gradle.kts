plugins {
    // Apply plugins for common configurations (e.g., Java application plugin)
    base // If you're using Kotlin, otherwise Java plugin
}

allprojects {
    group = "com.example"
    version = "1.0.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Common configuration for all submodules
    apply(plugin = "application")

    // dependencies {
    //     // Add common dependencies here, for example:
    //     testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    //     testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    // }

    // tasks.test {
    //     useJUnitPlatform() // Use JUnit for testing
    // }
}

// task("runServerThenClient") {
//     dependsOn(":server:run", ":client:run")

//     doLast {
//         println("Server and Client have run.")
//     }
// }