plugins {
    application
}

application {
    mainClass.set("com.example.client.Client")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    // testimplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.test {
    useJUnitPlatform()
}