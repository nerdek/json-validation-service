name := "json-validation-service"

version := "0.1"

scalaVersion := "2.13.9"
resolvers += "jitpack".at("https://jitpack.io")

libraryDependencies += "co.fs2"        %% "fs2-core"          % "3.3.0"
libraryDependencies += "co.fs2"        %% "fs2-io"            % "3.3.0"
libraryDependencies += "org.typelevel" %% "cats-core"         % "2.8.0"
libraryDependencies += "org.typelevel" %% "cats-effect"       % "3.3.14"
libraryDependencies += "io.circe"      %% "circe-fs2"         % "0.14.0"
libraryDependencies += "io.circe"      %% "circe-json-schema" % "0.2.0"

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val doobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)

val logging = Seq(
  "org.slf4j"                   % "slf4j-api"       % "2.0.3",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5",
  "ch.qos.logback"              % "logback-classic" % "1.4.3" % Runtime
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

libraryDependencies ++= logging

libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-core"              % "1.1.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % "1.1.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % "1.1.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.1.2"
libraryDependencies += "org.http4s"                  %% "http4s-blaze-server"     % "0.23.12"
libraryDependencies += "org.http4s"                  %% "http4s-circe"            % "0.23.16"

libraryDependencies += "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test

libraryDependencies += "com.github.pureconfig" %% "pureconfig"             % "0.17.1"
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
