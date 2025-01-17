# SqlDelight 2.0.x Postgresql simple migrations

https://github.com/cashapp/sqldelight

For the JDBC Driver in SqlDelight, there is no default migration file support 
 - see how it is built in for Sqlite [sqlite-driver](https://github.com/cashapp/sqldelight/blob/bd3cd6b2ca4c145a44686e85cfb4ed94e3513995/drivers/sqlite-driver/src/main/kotlin/app/cash/sqldelight/driver/jdbc/sqlite/JdbcSqliteSchema.kt#L20-L42)

For PostgreSql, using a simple schema to retrieve/store the migration version, the updated `sqm` files can be applied on application start up
- The below implementation is an example only to make it more obvious and could be factored into extension functions or other reusable code

```sql
 CREATE TABLE IF NOT EXISTS migrations (
    version INTEGER PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
```

Assumes the database schema is empty or the migrations table is empty or set to a version

Using migrations - it can assume the initial schema exists already.

The naming of the migrations files are zero index based as we start from nothing.

What is _confusing_ is that the migration numbering is such that, for example, `v1_add_table.sqm` means changes 
to move from version 1 to version 2 of the scheme and doesn't represent the initial version 1 schema.

`deriveSchemaFromMigrations` is enabled in build. This is used as SqlDelight is schema first and, for example,
because table column names, nullability can be changed the data class tables and queries will need to reflect the latest
version.

On build, SqlDelight will generate the database schema implementation code e.g. `griffio.queries.sqldelightpgsimplemigrations.SampleImpl` containing all the migration statements, in `build\generated\sqldelight\...`

The generated Schema is available e.g, `Sample.Schema.migrate(...)` to be executed with the desired start state and end state version number

On every application startup - attempt to create or migrate the database to the local migration version.

The initial migration version is zero as assumes that a database is empty.

* Transactional DDL with PostgreSql
  - We want each migration attempt to succeed or fail atomically, the generated Schema code doesn't use a transaction block
  - Wrap migrate with `sample.transaction` block as otherwise each statement would be run in own transaction
  - see https://wiki.postgresql.org/wiki/Transactional_DDL_in_PostgreSQL:_A_Competitive_Analysis

Version 2 (`1_add_table.sqm`) of the migration will fail due to the duplicate insert row - all the statements in the same transaction will roll back

A successful migration will result in the database migrations table with version = 2

```sql
CREATE TABLE AnotherTable (
  a TEXT
);

CREATE UNIQUE INDEX index_AnotherTable_a ON AnotherTable(a);

INSERT INTO AnotherTable (a) VALUES ('a');
INSERT INTO AnotherTable (a) VALUES ('b');
INSERT INTO AnotherTable (a) VALUES ('c');
INSERT INTO AnotherTable (a) VALUES ('a'); -- comment this out for migration to succeed
```

```bash
createdb sample_db &&
./gradlew build &&
./gradlew run
```

---

For more robust migrations and more complex configuration

see https://github.com/griffio/sqldelight-postgres-01 for Flyway migrations

see https://github.com/griffio/sqldelight-postgres-02 for Liquibase migrations
