package com.example.lambda

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.scanamo.DynamoFormat

case class User(id: String, name: String, email: String)

object User {
  implicit val decoder: Decoder[User] = deriveDecoder
  implicit val encoder: Encoder[User] = deriveEncoder
  // Scanamo format derivation might need specific imports or macros depending on version,
  // but for now we'll assume standard generic derivation or manual if needed.
  // implicit val dynamoFormat: DynamoFormat[User] = DynamoFormat[User]
}

case class DataRecord(id: String, content: String)

object DataRecord {
  implicit val decoder: Decoder[DataRecord] = deriveDecoder
  implicit val encoder: Encoder[DataRecord] = deriveEncoder
}
