package com.example.lambda

import zio._
import com.example.lambda.services._

trait BusinessLogic {
  def execute(input: String): Task[Unit]
}

object BusinessLogic {
  def execute(input: String): ZIO[BusinessLogic, Throwable, Unit] =
    ZIO.serviceWithZIO[BusinessLogic](_.execute(input))
}

case class BusinessLogicImpl(
    s3: S3Service,
    dynamo: DynamoService,
    kinesis: KinesisService,
    postgres: PostgresService,
    config: ConfigService,
    api: ApiService
) extends BusinessLogic {

  override def execute(input: String): Task[Unit] = {
    val logic = for {
      _ <- ZIO.logInfo(s"Starting processing for input: $input")

      // 1. Read Config
      param <- config.getParameter("/my-app/config/some-param")
      secret <- config.getSecret("my-app-secret")
      _ <- ZIO.logInfo(s"Loaded config: $param")

      // 2. Call External API
      apiData <- api.fetchData("https://httpbin.org/get")
      _ <- ZIO.logInfo("Fetched API data")

      // 3. Database Operations (Slick)
      _ <- postgres.addUser(User(input, "Test User", "test@example.com"))
      users <- postgres.getUsers
      _ <- ZIO.logInfo(s"Current users: $users")

      // 4. DynamoDB Operations (Scanamo)
      _ <- dynamo.putUser(
        "my-app-table",
        User(input, "Dynamo User", "dynamo@example.com")
      )
      userOpt <- dynamo.getUser("my-app-table", input)
      _ <- ZIO.logInfo(s"Dynamo User: $userOpt")

      // 5. S3 Operations
      _ <- s3.upload("my-app-bucket", s"data/$input.txt", apiData)
      _ <- ZIO.logInfo("Uploaded to S3")

      // 6. Kinesis Operations
      _ <- kinesis.putRecord("output-stream", input, s"Processed $input")
      _ <- ZIO.logInfo("Put record to Kinesis")

      // 7. Consume Kinesis Records (Demonstration)
      records <- kinesis.getRecords("input-stream")
      _ <- ZIO.logInfo(s"Consumed records from Kinesis: $records")

    } yield ()

    // Retry Logic
    logic
      .retry(
        (Schedule.exponential(1.second) && Schedule.recurs(3))
          .tapOutput { case (duration, count) =>
            ZIO.logWarning(
              s"Retry attempt ${count + 1}/3 after ${duration}. Retries remaining: ${2 - count}"
            )
          }
          .tapInput((e: Throwable) =>
            ZIO.logWarning(s"Retrying due to error: ${e.getMessage}")
          )
      )
      .tapError(e => ZIO.logError(s"Failed after retries: ${e.getMessage}"))
  }
}

object BusinessLogicImpl {
  val layer: ZLayer[
    S3Service
      with DynamoService
      with KinesisService
      with PostgresService
      with ConfigService
      with ApiService,
    Nothing,
    BusinessLogic
  ] = ZLayer.fromFunction(BusinessLogicImpl(_, _, _, _, _, _))
}
