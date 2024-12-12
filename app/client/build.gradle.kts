application {
    mainClass.set("com.example.client.Client")
}

dependencies {
    implementation(project(":app:common"))
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.example.client.Client"
    }
}