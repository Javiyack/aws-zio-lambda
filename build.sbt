val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "aws-lambda-zio-scala",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-streams" % "2.0.19",
      "dev.zio" %% "zio-json" % "0.6.2",
      "dev.zio" %% "zio-config" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC16",
      "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC16",

      // AWS SDK v2
      "software.amazon.awssdk" % "s3" % "2.21.15",
      "software.amazon.awssdk" % "dynamodb" % "2.21.15",
      "software.amazon.awssdk" % "kinesis" % "2.21.15",
      "software.amazon.awssdk" % "ssm" % "2.21.15",
      "software.amazon.awssdk" % "secretsmanager" % "2.21.15",
      "software.amazon.awssdk" % "lambda" % "2.21.15",

      // AWS Lambda Runtime
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
      "com.amazonaws" % "aws-lambda-java-events" % "3.11.3",

      // Slick & Postgres
      "com.typesafe.slick" %% "slick" % "3.5.0-M4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.5.0-M4",
      "org.postgresql" % "postgresql" % "42.6.0",

      // Scanamo (DynamoDB)
      "org.scanamo" %% "scanamo" % "1.0.0-M28",

      // STTP (HTTP Client)
      "com.softwaremill.sttp.client3" %% "zio" % "3.9.0",
      "com.softwaremill.sttp.client3" %% "circe" % "3.9.0",

      // Circe (JSON)
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.11",

      // Testing
      "dev.zio" %% "zio-test" % "2.0.19" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.19" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.19" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}
