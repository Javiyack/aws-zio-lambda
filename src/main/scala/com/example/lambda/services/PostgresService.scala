package com.example.lambda.services

import zio._
import slick.jdbc.PostgresProfile.api._
import com.example.lambda.User
import scala.concurrent.ExecutionContext

trait PostgresService {
  def getUsers: Task[Seq[User]]
  def addUser(user: User): Task[Int]
}

object PostgresService {
  def getUsers: ZIO[PostgresService, Throwable, Seq[User]] =
    ZIO.serviceWithZIO[PostgresService](_.getUsers)

  def addUser(user: User): ZIO[PostgresService, Throwable, Int] =
    ZIO.serviceWithZIO[PostgresService](_.addUser(user))
}

// Slick Table Definition
class UsersTable(tag: slick.lifted.Tag) extends Table[User](tag, "users") {
  def id = column[String]("id", O.PrimaryKey)
  def name = column[String]("name")
  def email = column[String]("email")
  def * = (id, name, email) <> ((User.apply _).tupled, User.unapply)
}

case class PostgresServiceImpl(db: Database) extends PostgresService {
  val users = TableQuery[UsersTable]

  override def getUsers: Task[Seq[User]] =
    ZIO.fromFuture { implicit ec =>
      db.run(users.result)
    }

  override def addUser(user: User): Task[Int] =
    ZIO.fromFuture { implicit ec =>
      db.run(users += user)
    }
}

object PostgresServiceImpl {
  // In a real app, you'd probably load config from ZIO Config
  // For now, we'll assume a Database is provided
  val layer: ZLayer[Database, Nothing, PostgresService] =
    ZLayer.fromFunction(PostgresServiceImpl(_))
}
