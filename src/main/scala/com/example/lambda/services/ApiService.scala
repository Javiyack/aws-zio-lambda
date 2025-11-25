package com.example.lambda.services

import zio._
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import io.circe.generic.auto._

trait ApiService {
  def fetchData(url: String): Task[String]
}

object ApiService {
  def fetchData(url: String): ZIO[ApiService, Throwable, String] =
    ZIO.serviceWithZIO[ApiService](_.fetchData(url))
}

case class ApiServiceImpl(backend: SttpBackend[Task, Any]) extends ApiService {
  override def fetchData(url: String): Task[String] =
    basicRequest
      .get(uri"$url")
      .send(backend)
      .flatMap { response =>
        response.body match {
          case Right(body) => ZIO.succeed(body)
          case Left(error) =>
            ZIO.fail(new RuntimeException(s"API Error: $error"))
        }
      }
}

object ApiServiceImpl {
  val layer: ZLayer[Any, Throwable, ApiService] =
    ZLayer.scoped {
      HttpClientZioBackend.scoped().map(ApiServiceImpl(_))
    }
}
