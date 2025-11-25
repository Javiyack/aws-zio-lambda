package com.example.lambda

import zio._
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import slick.jdbc.PostgresProfile.api._
import com.example.lambda.services._
import java.net.URI

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class LambdaHandler
    extends RequestHandler[java.util.Map[String, Object], String] {

  // Runtime for executing ZIO effects
  private val runtime = Runtime.default

  // AWS Clients Layer (configured for LocalStack if needed, or real AWS)
  // In a real lambda, you might use ZIO AWS or standard SDK builders
  // For this example, we'll use a simple layer construction

  val awsClientsLayer = ZLayer.make[
    S3AsyncClient
      with DynamoDbAsyncClient
      with KinesisAsyncClient
      with SsmAsyncClient
      with SecretsManagerAsyncClient
  ](
    ZLayer.succeed(
      S3AsyncClient
        .builder()
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()
    ), // LocalStack endpoint
    ZLayer.succeed(
      DynamoDbAsyncClient
        .builder()
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()
    ),
    ZLayer.succeed(
      KinesisAsyncClient
        .builder()
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()
    ),
    ZLayer.succeed(
      SsmAsyncClient
        .builder()
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()
    ),
    ZLayer.succeed(
      SecretsManagerAsyncClient
        .builder()
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()
    )
  )

  // Database Layer
  val dbLayer = ZLayer.succeed(
    Database.forURL(
      "jdbc:postgresql://localhost:5432/mydatabase",
      user = "myuser",
      password = "mypassword",
      driver = "org.postgresql.Driver"
    )
  )

  // Application Layer
  val appLayer = ZLayer.make[BusinessLogic](
    BusinessLogicImpl.layer,
    S3ServiceImpl.layer,
    DynamoServiceImpl.layer,
    KinesisServiceImpl.layer,
    PostgresServiceImpl.layer,
    ConfigServiceImpl.layer,
    ApiServiceImpl.layer,
    awsClientsLayer,
    dbLayer
  )

  override def handleRequest(
      input: java.util.Map[String, Object],
      context: Context
  ): String = {
    // Extract ID from input map, defaulting to "default-id" if not present
    val id = Option(input.get("id")).map(_.toString).getOrElse("default-id")

    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe
        .run(
          BusinessLogic
            .execute(id)
            .provide(appLayer)
        )
        .getOrThrowFiberFailure()
    }

    s"Processed request for ID: $id"
  }
}

object LambdaHandlerLocal extends ZIOAppDefault {
  // Main entry point for local testing
  override def run = {
    // Re-using the logic from the class instance for local run would require refactoring layers to be shared or duplicated.
    // For simplicity, we'll just instantiate the handler logic or duplicate the layer definition here if we want to keep it self-contained.
    // But better yet, let's just keep the local runner simple as before, or instantiate the class.

    val handler = new LambdaHandler()

    ZIO
      .attempt {
        handler.handleRequest(
          java.util.Collections.singletonMap("id", "local-test-id"),
          null
        )
      }
      .flatMap(Console.printLine(_))
  }
}
