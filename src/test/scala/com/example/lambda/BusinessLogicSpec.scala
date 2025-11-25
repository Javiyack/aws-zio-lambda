package com.example.lambda

import zio._
import zio.test._
import zio.test.Assertion._
import com.example.lambda.services._

object BusinessLogicSpec extends ZIOSpecDefault {

  // Mocks would be ideal here, but for simplicity in this environment,
  // we'll create stub implementations or use ZLayer.succeed with dummy data.

  val stubS3 = ZLayer.succeed(new S3Service {
    def upload(bucket: String, key: String, content: String): Task[Unit] =
      ZIO.unit
    def download(bucket: String, key: String): Task[String] =
      ZIO.succeed("content")
  })

  val stubDynamo = ZLayer.succeed(new DynamoService {
    def putUser(table: String, user: User): Task[Unit] = ZIO.unit
    def getUser(table: String, id: String): Task[Option[User]] =
      ZIO.succeed(Some(User(id, "Stub", "stub@example.com")))
  })

  val stubKinesis = ZLayer.succeed(new KinesisService {
    def putRecord(
        streamName: String,
        partitionKey: String,
        data: String
    ): Task[Unit] = ZIO.unit
    def getRecords(streamName: String): Task[List[String]] =
      ZIO.succeed(List("mock-record"))
  })

  val stubPostgres = ZLayer.succeed(new PostgresService {
    def getUsers: Task[Seq[User]] = ZIO.succeed(Seq.empty)
    def addUser(user: User): Task[Int] = ZIO.succeed(1)
  })

  val stubConfig = ZLayer.succeed(new ConfigService {
    def getParameter(name: String): Task[String] =
      ZIO.succeed("xxx-param-value")
    def getSecret(secretId: String): Task[String] = ZIO.succeed("secret-value")
  })

  val stubApi = ZLayer.succeed(new ApiService {
    def fetchData(url: String): Task[String] =
      ZIO.succeed("{\"data\": \"api-response\"}")
  })

  def spec = suite("BusinessLogicSpec")(
    test("execute runs successfully with all services") {
      val effect = BusinessLogic.execute("test-id")
      assertZIO(effect)(isUnit)
    }.provide(
      BusinessLogicImpl.layer,
      stubS3,
      stubDynamo,
      stubKinesis,
      stubPostgres,
      stubConfig,
      stubApi
    )
  )
}
