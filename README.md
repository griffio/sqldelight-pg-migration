# SqlDelight 2.0.x Postgresql simple migrations

https://github.com/cashapp/sqldelight

Using a simple schema to store the migration version that matches the SqlDelight migration

```sql
 CREATE TABLE IF NOT EXISTS migrations (
    version INTEGER PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
```

Assumes the database schema is empty or the migrations table is empty or set to a version

Using migrations - it can assume the initial schema exists already.

The naming of the migrations files are zero index based as we start from nothing.

What is `confusing` is that the migration numbering is such that, for example, `v1_add_table.sqm` means changes 
to move from version 1 to version 2 of the scheme and doesn't represent the initial version 1 schema.

`deriveSchemaFromMigrations` is enabled in build

On every application startup - attempt to create or migrate the database to the local migration version

The initial migration version is zero as assumes that database is empty 

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

see https://github.com/griffio/sqldelight-postgres-01 for Flyway migrations 
see https://github.com/griffio/sqldelight-postgres-02 for Liquibase migrations

```bash
createdb sample_db &&
./gradlew build &&
./gradlew run
```
