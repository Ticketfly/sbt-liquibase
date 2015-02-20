Liquibase plugin for sbt 0.13+
==============================

# Instructions for use:
### Step 1: Include the plugin in your build

Add the following to your `project/plugins.sbt`:

## sbt-0.13+

    addSbtPlugin("com.t" % "sbt-liquibase" % "0.1")

### Step 2: Add sbt-liquibase settings to your build

Add the following to your 'build.sbt' ( if you are using build.sbt )


    import com.ticketfly.sbt.liquibase.LiquibasePlugin

    seq(LiquibasePlugin.liquibaseSettings: _*)

    liquibaseUsername := ""

    liquibasePassword := ""

    liquibaseDriver   := "com.mysql.jdbc.Driver"

    liquibaseUrl      := "jdbc:mysql://localhost:3306/test_db?createDatabaseIfNotExist=true"

Or if you are using a build object extending from Build:icketfly

    import sbt._
    import Keys._
    import com.github.bigtoast.sbtliquibase.LiquibasePlugin._

    class MyBuildThatHasntDrankTheNoSQLKoolAid extends Build {
         lazy val seniorProject = Project("hola", file("."), settings = Defaults.defaultSettings ++ liquibaseSettings ++ Seq (
              liquibaseUsername := "dbusername",
              liquibasePassword := "kittensareevil"
              /* lots more liquibase settings available */
         )
    }


## Settings

<table>
    <tr>
        <td> <b>liquibaseUsername</b> </td>
        <td>Username for the database. This defaults to blank.</td>
        <td> liquibaseUsername := "asdf" </td>
    </tr>
    <tr>
        <td> <b>liquibasePassword</b> </td>
        <td>Password for the database. This defaults to blank.</td>
        <td> liquibasePassword := "secretstuff" </td>
    </tr>
    <tr>
        <td> <b>liqubaseDriver</b> </td>
        <td>Database driver classname. There is no default.</td>
        <td> liquibaseDriver := "com.mysql.jdbc.Driver" </td>
    </tr>
    <tr>
        <td> <b>liquibaseUrl</b> </td>
        <td>Database connection uri. There is no default.</td>
        <td> liquibaseUrl := "jdbc:mysql://localhost:3306/mydb" </td>
    </tr>
    <tr>
        <td> <b>liquibaseDefaultSchemaName</b> </td>
        <td>Default schema name for the db if it isn't defined in the uri. This defaults to null.</td>
        <td> liquibaseDefaultSchemaName := "dbname" </td>
    </tr>
    <tr>
        <td> <b>liquibaseChangelog</b> </td>
        <td>Full path to your changelog file. This defaults 'src/main/migrations/changelog.xml'.</td>
        <td> liquibaseChangelog := "other/path/dbchanges.xml" </td>
    </tr>
</table>

## Tasks

<table>
    <tr>
        <td> <b>liquibaseUpdate</b> </td>
        <td>Run the liquibase migration</td>
    </tr>
    <tr>
        <td><b>liquibaseStatus</b></td>
        <td>Print count of yet to be run changesets</td>
    </tr>
    <tr>
        <td><b>liquibaseClearChecksums</b></td>
        <td>Removes all saved checksums from database log. Useful for 'MD5Sum Check Failed' errors</td>
    </tr>
    <tr>
        <td><b>liquibaseListLocks</b></td>
        <td>Lists who currently has locks on the database changelog</td>
    </tr>
    <tr>
        <td><b>liquibaseRelease-locks</b></td>
        <td>Releases all locks on the database changelog.</td>
    </tr>
    <tr>
        <td><b>liquibaseValidateChangelog</b></td>
        <td>Checks changelog for errors.</td>
    </tr>
    <tr>
        <td><b>liquibaseDbDiff</b></td>
        <td>( this isn't implemented yet ) Generate changeSet(s) to make Test DB match Development</td>
    </tr>
    <tr>
        <td><b>liquibaseDbDoc</b></td>
        <td>Generates Javadoc-like documentation based on current database and change log</td>
    </tr>
    <tr>
        <td><b>liquibaseGenerateChangelog</b></td>
        <td>Writes Change Log XML to copy the current state of the database to the file defined in the changelog setting</td>
    </tr>
    <tr>
        <td><b>liquibaseChangelogSyncSql</b></td>
        <td>Writes SQL to mark all changes as executed in the database to STDOUT</td>
    </tr>
    <tr>
        <td><b>liquibaseTag</b> {tag}</td>
        <td>Tags the current database state for future rollback with {tag}</td>
    </tr>
    <tr>
        <td><b>liquibaseRollback</b> {tag}</td>
        <td>Rolls back the database to the the state is was when the {tag} was applied.</td>
    </tr>
    <tr>
        <td><b>liquibaseRollbackSql</b> {tag}</td>
        <td>Writes SQL to roll back the database to that state it was in when the {tag} was applied to STDOUT</td>
    </tr>
    <tr>
        <td><b>liquibaseRollbackCount</b> {int}</td>
        <td>Rolls back the last {int i} change sets applied to the database</td>
    </tr>
    <tr>
        <td><b>liquibaseRollbackSqlCount</b> {int}</td>
        <td>Writes SQL to roll back the last {int i} change sets to STDOUT applied to the database</td>
    </tr>
    <tr>
        <td><b>liquibaseRollbackToDate</b> { yyyy-MM-dd HH:mm:ss }</td>
        <td>Rolls back the database to the the state it was at the given date/time. Date Format: yyyy-MM-dd HH:mm:ss</td>
    </tr>
    <tr>
        <td><b>liquibaseRollbackToDateSql</b> { yyyy-MM-dd HH:mm:ss }</td>
        <td>Writes SQL to roll back the database to that state it was in at the given date/time version to STDOUT</td>
    </tr>
    <tr>
        <td><b>liquibaseFutureRollbackSql</b></td>
        <td>Writes SQL to roll back the database to the current state after the changes in the changelog have been applied.</td>
    </tr>
    <tr>
        <td><b>liquibaseDropAll</b></td>
        <td>Drop all tables</td>
    </tr>
</table>


Acknoledgements
---------------

This plugin is largerly a port of [https://github.com/bigtoast/sbt-liquibase|sbt-liquibase] by @bigtoast


