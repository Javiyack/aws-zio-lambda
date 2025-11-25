package com.example.lambda.services

import zio._
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.generic.auto._
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import com.example.lambda.User

trait DynamoService {
  def putUser(table: String, user: User): Task[Unit]
  def getUser(table: String, id: String): Task[Option[User]]
}

object DynamoService {
  def putUser(table: String, user: User): ZIO[DynamoService, Throwable, Unit] =
    ZIO.serviceWithZIO[DynamoService](_.putUser(table, user))

  def getUser(
      table: String,
      id: String
  ): ZIO[DynamoService, Throwable, Option[User]] =
    ZIO.serviceWithZIO[DynamoService](_.getUser(table, id))
}

case class DynamoServiceImpl(client: DynamoDbAsyncClient)
    extends DynamoService {
  // ScanamoAsyncInterpreter requires a DynamoDbAsyncClient
  // We need to adapt Scanamo's Future to ZIO

  // Note: Scanamo 1.0.0-M28 might have slightly different API for Async
  // Assuming standard usage with ScanamoAsync

  import org.scanamo.ops.ScanamoOps

  // Helper to execute Scanamo ops
  private def exec[A](op: ScanamoOps[A]): Task[A] = {
    ZIO.fromFuture { implicit ec =>
      ScanamoAsync(client).exec(op)
    }
  }

  override def putUser(table: String, user: User): Task[Unit] =
    exec(Table[User](table).put(user))

  override def getUser(table: String, id: String): Task[Option[User]] =
    exec(Table[User](table).get("id" === id)).map(_.flatMap(_.toOption))
}

object DynamoServiceImpl {
  val layer: ZLayer[DynamoDbAsyncClient, Nothing, DynamoService] =
    ZLayer.fromFunction(DynamoServiceImpl(_))
}
