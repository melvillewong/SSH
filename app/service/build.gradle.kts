dependencies {
    implementation(project(":app:common"))
    implementation("org.postgresql:postgresql:42.7.3")                  // PostgreSQL JDBC driver
    testImplementation("org.mockito:mockito-core:4.0.0")                // Add Mockito
}