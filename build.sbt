name := """getbike2"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.mockito" % "mockito-core" % "2.2.22",
  "com.google.code.gson" % "gson" % "1.7.1"
)

jacoco.settings
parallelExecution in jacoco.Config := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")