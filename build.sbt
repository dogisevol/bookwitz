name := """bookwitz"""

organization in ThisBuild := "io.bookwitz"

version in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := "2.11.2"

startYear := Some(2013)

homepage := Some(url("https://blablabla"))

licenses := Seq("GNU AFFERO GENERAL PUBLIC LICENSE, Version 3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))

resolvers in ThisBuild += Resolver.url("Edulify Repository", url("http://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)

lazy val web = (project in file("modules/web"))
  .enablePlugins(PlayScala)
.dependsOn(users)

lazy val users = (project in file("modules/users"))
  .enablePlugins(PlayScala)

lazy val bookwitz = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(web, users)
  .dependsOn(web)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "angularjs" % "1.5.7",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "requirejs" % "2.1.14-1",
  "org.webjars" % "angular-ui" % "0.4.0-3",
  "org.webjars" % "ui-grid" % "3.1.1",
  "org.webjars" % "angular-file-upload" % "11.0.0"
)

libraryDependencies in ThisBuild ++= Seq(
  cache,
  jdbc,
  "javax.inject" % "javax.inject" % "1",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "2.0.4",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.4",
  "com.edulify" %% "play-hikaricp" % "1.4.1",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "org.joda" % "joda-convert" % "1.6",
  "com.jsuereth" %% "scala-arm" % "1.4",
  "ws.securesocial" %% "securesocial" % "3.0-M1",
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.mongodb" %% "casbah" % "2.8.2",
  "com.novus" %% "salat" % "1.9.9"
)

// http://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"


pipelineStages := Seq(rjs, digest, gzip)

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  //"-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-language:reflectiveCalls"
)

fork in run := false

unmanagedJars in Compile ++= {
  val base = baseDirectory.value
  val baseDirectories = (base / "lib")
  val customJars = (baseDirectories ** "*.jar")
  customJars.classpath
}

mappings in Universal ++= {
  val resourcesDir = baseDirectory.value/"resources"
  for {
    file <- (resourcesDir ** AllPassFilter).get
    relative <- file.relativeTo(resourcesDir.getParentFile)
    mapping = file -> relative.getPath
  } yield mapping
}
