plugins {
    // Apply plugins for common configurations (e.g., Java application plugin)
    base
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

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.10.2")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform() // Use JUnit for testing
    }

    // tasks.withType<Test> {
    //     enabled = false
    // }
}
