plugins {
    application
}

application {
    mainClass.set("com.example.server.Server")
}

dependencies {
    implementation(project(":app:service"))
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    // testimplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.test {
    useJUnitPlatform()
}