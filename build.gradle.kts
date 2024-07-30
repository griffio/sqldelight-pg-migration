plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.sqldelight)
    application
}

group = "griffio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.sqldelight.jdbc.driver)
    api(libs.sqldelight.postgresql.dialect)
    implementation(libs.postgresql.jdbc.driver)
    testImplementation(kotlin("test"))
}

sqldelight {
    databases {
        create("Sample") {
            deriveSchemaFromMigrations.set(true)
            packageName.set("griffio.queries")
            dialect(libs.sqldelight.postgresql.dialect)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("griffio.MainKt")
}
