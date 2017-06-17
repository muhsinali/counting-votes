
name := """countMeUpKata"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "3.4.2",
  "org.mongodb.morphia" % "morphia" % "1.3.2",
  "redis.clients" % "jedis" % "2.9.0",

// For tests
  "info.cukes" % "cucumber-java8" % "1.2.5",
  "info.cukes" % "cucumber-junit" % "1.2.5",
  "junit" % "junit" % "4.12"
)

// For code coverage using JaCoCo
jacoco.settings
