package com.example.lambda.services

import zio._
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

trait ConfigService {
  def getParameter(name: String): Task[String]
  def getSecret(secretId: String): Task[String]
}

object ConfigService {
  def getParameter(name: String): ZIO[ConfigService, Throwable, String] =
    ZIO.serviceWithZIO[ConfigService](_.getParameter(name))

  def getSecret(secretId: String): ZIO[ConfigService, Throwable, String] =
    ZIO.serviceWithZIO[ConfigService](_.getSecret(secretId))
}

case class ConfigServiceImpl(
    ssmClient: SsmAsyncClient,
    secretsClient: SecretsManagerAsyncClient
) extends ConfigService {
  override def getParameter(name: String): Task[String] =
    ZIO
      .fromCompletableFuture(
        ssmClient.getParameter(GetParameterRequest.builder().name(name).build())
      )
      .map(_.parameter().value())

  override def getSecret(secretId: String): Task[String] =
    ZIO
      .fromCompletableFuture(
        secretsClient.getSecretValue(
          GetSecretValueRequest.builder().secretId(secretId).build()
        )
      )
      .map(_.secretString())
}

object ConfigServiceImpl {
  val layer: ZLayer[
    SsmAsyncClient with SecretsManagerAsyncClient,
    Nothing,
    ConfigService
  ] =
    ZLayer.fromFunction(ConfigServiceImpl(_, _))
}
