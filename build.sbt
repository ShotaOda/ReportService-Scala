name := """play-scala-change"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
  "mysql" % "mysql-connector-java" % "5.1.24",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.1.1",

  // joda
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.2.0",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  //"org.slf4j" % "slf4j-nop" % "1.6.4",

  // mail
  "javax.mail" % "javax.mail-api" % "1.5.1",
  "javax.mail" % "mail" % "1.5.0-b01",
  "com.github.pathikrit" %% "better-files" % "2.14.0",

  //config
  "com.typesafe" % "config" % "1.2.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

fork in run := false

slick <<= slickCodeGenTask // register manual sbt command

lazy val slick = TaskKey[Unit]("gen-tables")
lazy val slickCodeGenTask = (dependencyClasspath in Compile, runner in Compile, streams) map {(cp, r, s) =>
  val slickDriver = "slick.driver.MySQLDriver"
  val jdbcDriver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://localhost/testdb"
  val outputDir = "app"
  val pkg = "models"
  val usr = "root"
  val password = ""

  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg, usr, password), s.log))
  println("output file is -> " + outputDir + "/models/Tables.scala")
}
