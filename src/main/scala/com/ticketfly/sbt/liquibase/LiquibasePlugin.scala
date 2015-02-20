package com.ticketfly.sbt.liquibase

import java.io.PrintStream
import java.text.SimpleDateFormat

import liquibase.Liquibase
import liquibase.database.Database
import liquibase.integration.commandline.CommandLineUtils
import liquibase.resource.FileSystemResourceAccessor
import sbt.Keys._
import sbt._
import sbt.classpath._

object LiquibasePlugin extends Plugin {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  val liquibaseUpdate             = TaskKey[Unit]("liquibaseUpdate", "Run a liquibase migration")
  val liquibaseStatus             = TaskKey[Unit]("liquibaseStatus", "Print count of unrun change sets")
  val liquibaseClearChecksums     = TaskKey[Unit]("liquibaseClearChecksums", "Removes all saved checksums from database log. Useful for 'MD5Sum Check Failed' errors")
  val liquibaseListLocks          = TaskKey[Unit]("liquibaseListLocks", "Lists who currently has locks on the database changelog")
  val liquibaseReleaseLocks       = TaskKey[Unit]("liquibaseReleaseLocks", "Releases all locks on the database changelog")
  val liquibaseValidateChangelog  = TaskKey[Unit]("liquibaseValidateChangelog", "Checks changelog for errors")
  val liquibaseTag                = InputKey[Unit]("liquibaseTag", "Tags the current database state for future rollback")
  val liquibaseDbDiff             = TaskKey[Unit]("liquibaseDbDiff", "( this isn't implemented yet ) Generate changeSet(s) to make Test DB match Development")
  val liquibaseDbDoc              = TaskKey[Unit]("liquibaseDbDoc", "Generates Javadoc-like documentation based on current database and change log")
  val liquibaseGenerateChangelog  = TaskKey[Unit]("liquibaseGenerateChangelog", "Writes Change Log XML to copy the current state of the database to standard out")
  val liquibaseChangelogSyncSql   = TaskKey[Unit]("liquibaseChangelogSyncSql", "Writes SQL to mark all changes as executed in the database to STDOUT")
  val liquibaseDropAll            = TaskKey[Unit]("liquibaseDropAll", "Drop all database objects owned by user")

  val liquibaseRollback           = InputKey[Unit]("liquibaseRollback", "<tag> Rolls back the database to the the state is was when the tag was applied")
  val liquibaseRollbackSql        = InputKey[Unit]("liquibaseRollbackSql", "<tag> Writes SQL to roll back the database to that state it was in when the tag was applied to STDOUT")
  val liquibaseRollbackCount      = InputKey[Unit]("liquibaseRollbackCount", "<num>Rolls back the last <num> change sets applied to the database")
  val liquibaseRollbackCountSql   = InputKey[Unit]("liquibaseRollbackCountSql", "<num> Writes SQL to roll back the last <num> change sets to STDOUT applied to the database")
  val liquibaseRollbackToDate     = InputKey[Unit]("liquibaseRollbackToDate", "<date> Rolls back the database to the the state is was at the given date/time. Date Format: yyyy-MM-dd HH:mm:ss")
  val liquibaseRollbackToDateSql  = InputKey[Unit]("liquibaseRollbackToDateSql", "<date> Writes SQL to roll back the database to that state it was in at the given date/time version to STDOUT")
  val liquibaseFutureRollbackSql  = InputKey[Unit]("liquibaseFutureRollbackSql", " Writes SQL to roll back the database to the current state after the changes in the changelog have been applied")

  val liquibaseChangelog          = SettingKey[String]("liquibaseChangelog", "This is your liquibase changelog file to run.")
  val liquibaseUrl                = SettingKey[String]("liquibaseUrl", "The url for liquibase")
  val liquibaseUsername           = SettingKey[String]("liquibaseUsername", "username yo.")
  val liquibasePassword           = SettingKey[String]("liquibasePassword", "password")
  val liquibaseDriver             = SettingKey[String]("liquibaseDriver", "driver")
  val liquibaseDefaultSchemaName  = SettingKey[String]("liquibaseDefaultSchemaName", "default schema name")
  val liquibaseContext            = SettingKey[String]("liquibaseContext", "changeSet contexts to execute")

  lazy val liquibaseDatabase      = TaskKey[Database]("liquibaseDatabase", "the database")
  lazy val liquibase              = TaskKey[Liquibase]("liquibase", "liquibase object")

  lazy val liquibaseSettings: Seq[Setting[_]] = Seq[Setting[_]](
    liquibaseDefaultSchemaName  := "liquischema",
    liquibaseChangelog          := "src/main/migrations/changelog.xml",
    liquibaseContext            := "",

    liquibaseDatabase <<= (liquibaseUrl, liquibaseUsername, liquibasePassword, liquibaseDriver, liquibaseDefaultSchemaName, fullClasspath in Runtime) map {
      (url: String, uname: String, pass: String, driver: String, schemaName: String, cpath) =>
        //CommandLineUtils.createDatabaseObject( ClasspathUtilities.toLoader(cpath.map(_.data)) ,url, uname, pass, driver, schemaName, null,null)
        CommandLineUtils.createDatabaseObject(ClasspathUtilities.toLoader(cpath.map(_.data)), url, uname, pass, driver, null, null, null)
    },

    liquibase <<= (liquibaseChangelog, liquibaseDatabase) map {
      (cLog: String, dBase: Database) =>
        new Liquibase(cLog, new FileSystemResourceAccessor, dBase)
    },

    liquibaseUpdate <<= (liquibase, liquibaseContext) map {
      (liquibase: Liquibase, context: String) =>
        liquibase.update(context)
    },

    liquibaseStatus <<= liquibase map {
      _.reportStatus(true, null, new LoggerWriter(ConsoleLogger()))
    },

    liquibaseClearChecksums <<= liquibase map {
      _.clearCheckSums()
    },

    liquibaseListLocks <<= (streams, liquibase) map { (out, lbase) => lbase.reportLocks(new PrintStream(out.binary()))},
    liquibaseReleaseLocks <<= (streams, liquibase) map { (out, lbase) => lbase.forceReleaseLocks()},
    liquibaseValidateChangelog <<= (streams, liquibase) map { (out, lbase) => lbase.validate()},
    liquibaseDbDoc <<= (streams, liquibase, target) map { (out, lbase, tdir) =>
      lbase.generateDocumentation(tdir / "liquibase-doc" absolutePath)
      out.log("Documentation generated in %s".format(tdir / "liquibase-doc" absolutePath))
    },

    liquibaseRollback <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(args.head, null)
        out.log("Rolled back to tag %s".format(args.head))
      }
    },

    liquibaseRollbackCount <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(args.head.toInt, null)
        out.log("Rolled back to count %s".format(args.head))
      }
    },

    liquibaseRollbackSql <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(args.head, null, out.text())
      }
    },

    liquibaseRollbackCountSql <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(args.head.toInt, null, out.text())
      }
    },

    liquibaseRollbackToDate <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(dateFormat.parse(args.mkString(" ")), null)
      }
    },

    liquibaseRollbackToDateSql <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.rollback(dateFormat.parse(args.mkString(" ")), null, out.text())
      }
    },

    liquibaseFutureRollbackSql <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.futureRollbackSQL(null, out.text())
      }
    },

    liquibaseTag <<= inputTask { (argTask) =>
      (streams, liquibase, argTask) map { (out, lbase, args: Seq[String]) =>
        lbase.tag(args.head)
        out.log("Tagged db with %s for future rollback if needed".format(args.head))
      }
    },

    liquibaseGenerateChangelog <<= (streams, liquibase, liquibaseChangelog, liquibaseDefaultSchemaName, baseDirectory) map { (out, lbase, clog, sname, bdir) =>
      //CommandLineUtils.doGenerateChangeLog(clog, lbase.getDatabase(), sname, null,null,null, bdir / "src" / "main" / "migrations" absolutePath )
      CommandLineUtils.doGenerateChangeLog(clog, lbase.getDatabase(), null, null, null, null, bdir / "src" / "main" / "migrations" absolutePath)
    },

    liquibaseChangelogSyncSql <<= (streams, liquibase) map { (out, lbase) =>
      lbase.changeLogSync(null, out.text())
    },

    liquibaseDropAll <<= liquibase map {
      _.dropAll()
    }
  )


}
