package griffio

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import griffio.queries.Sample
import org.postgresql.ds.PGSimpleDataSource

fun getSqlDriver(): SqlDriver {
    val datasource = PGSimpleDataSource()
    datasource.setURL("jdbc:postgresql://localhost:5432/sample_db")
    datasource.applicationName = "App Main"
    return datasource.asJdbcDriver()
}

fun migrateIfNeeded(driver: SqlDriver, sample: Sample) {

    driver.execute(
        null, """
        CREATE TABLE IF NOT EXISTS migrations (
            version INTEGER PRIMARY KEY,
            applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
    """.trimIndent(), 0
    )

    val oldVersion =
        driver.executeQuery(
            null,
            "SELECT COALESCE(MAX(version), 0) AS current_version FROM migrations", {
                QueryResult.Value(if (it.next().value) it.getLong(0)!! else 0L)
            }, 0
        )

    val newVersion: Long = Sample.Schema.version

    when {
        oldVersion.value == 0L -> {
            println("Creating DB from version ${oldVersion.value} to $newVersion")
            sample.transaction { // Use transactional DDL
                // Sample.Schema.create doesn't include DML (e.g inserts) only DDL statements
                Sample.Schema.migrate(driver, oldVersion.value, newVersion) // migrate includes inserts
                driver.execute(null, "INSERT INTO migrations (version) VALUES ($newVersion);", 0)
            }
        }

        newVersion > oldVersion.value -> {
            println("Migrating DB from version $oldVersion to $newVersion!")
            sample.transaction { // Use transactional DDL
                Sample.Schema.migrate(driver, oldVersion.value, newVersion) // migrate includes inserts
                driver.execute(null, "INSERT INTO migrations (version) VALUES ($newVersion);", 0)
            }
        }

        else -> println("Migration not needed: database migrations ${oldVersion.value} / local migrations $newVersion")
    }
}

fun main() {

    val driver = getSqlDriver()
    val sample = Sample(driver)

    migrateIfNeeded(driver, sample)

    // sample do query
}
