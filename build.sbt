import bintray.Keys._

sbtPlugin := true

organization := "com.ticketfly"

name := "sbt-liquibase"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies += "org.liquibase" % "liquibase-core" % "3.3.2"

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

// This is an example.  bintray-sbt requires licenses to be specified
// (using a canonical name).
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization in bintray := None
