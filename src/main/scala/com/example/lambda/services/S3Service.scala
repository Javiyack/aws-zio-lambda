package com.example.lambda.services

import zio._
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{
  PutObjectRequest,
  GetObjectRequest
}
import software.amazon.awssdk.core.async.{
  AsyncRequestBody,
  AsyncResponseTransformer
}
import java.util.concurrent.CompletableFuture
import scala.jdk.FutureConverters._

trait S3Service {
  def upload(bucket: String, key: String, content: String): Task[Unit]
  def download(bucket: String, key: String): Task[String]
}

object S3Service {
  def upload(
      bucket: String,
      key: String,
      content: String
  ): ZIO[S3Service, Throwable, Unit] =
    ZIO.serviceWithZIO[S3Service](_.upload(bucket, key, content))

  def download(bucket: String, key: String): ZIO[S3Service, Throwable, String] =
    ZIO.serviceWithZIO[S3Service](_.download(bucket, key))
}

case class S3ServiceImpl(client: S3AsyncClient) extends S3Service {
  override def upload(
      bucket: String,
      key: String,
      content: String
  ): Task[Unit] =
    ZIO
      .fromCompletableFuture(
        client.putObject(
          PutObjectRequest.builder().bucket(bucket).key(key).build(),
          AsyncRequestBody.fromString(content)
        )
      )
      .unit

  override def download(bucket: String, key: String): Task[String] =
    ZIO
      .fromCompletableFuture(
        client.getObject(
          GetObjectRequest.builder().bucket(bucket).key(key).build(),
          AsyncResponseTransformer.toBytes()
        )
      )
      .map(_.asUtf8String())
}

object S3ServiceImpl {
  val layer: ZLayer[S3AsyncClient, Nothing, S3Service] =
    ZLayer.fromFunction(S3ServiceImpl(_))
}
