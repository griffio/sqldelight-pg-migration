plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "sqldelight-pg-simple-migrations"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val vSqlDelight = "2.0.2"
            plugin("kotlin", "org.jetbrains.kotlin.jvm").version("1.9.20")
            plugin("sqldelight", "app.cash.sqldelight").version(vSqlDelight)
            library("sqldelight-jdbc-driver", "app.cash.sqldelight:jdbc-driver:$vSqlDelight")
            library("sqldelight-postgresql-dialect", "app.cash.sqldelight:postgresql-dialect:$vSqlDelight")
            library("postgresql-jdbc-driver", "org.postgresql:postgresql:42.5.4")
        }
    }
}
