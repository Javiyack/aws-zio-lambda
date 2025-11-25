package com.example.lambda.services

import zio._
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.kinesis.model.{
  PutRecordRequest,
  GetShardIteratorRequest,
  GetRecordsRequest,
  ShardIteratorType
}
import software.amazon.awssdk.core.SdkBytes
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._

trait KinesisService {
  def putRecord(
      streamName: String,
      partitionKey: String,
      data: String
  ): Task[Unit]
  def getRecords(streamName: String): Task[List[String]]
}

object KinesisService {
  def putRecord(
      streamName: String,
      partitionKey: String,
      data: String
  ): ZIO[KinesisService, Throwable, Unit] =
    ZIO.serviceWithZIO[KinesisService](
      _.putRecord(streamName, partitionKey, data)
    )

  def getRecords(
      streamName: String
  ): ZIO[KinesisService, Throwable, List[String]] =
    ZIO.serviceWithZIO[KinesisService](_.getRecords(streamName))
}

case class KinesisServiceImpl(client: KinesisAsyncClient)
    extends KinesisService {
  override def putRecord(
      streamName: String,
      partitionKey: String,
      data: String
  ): Task[Unit] =
    ZIO
      .fromCompletableFuture(
        client.putRecord(
          PutRecordRequest
            .builder()
            .streamName(streamName)
            .partitionKey(partitionKey)
            .data(SdkBytes.fromString(data, StandardCharsets.UTF_8))
            .build()
        )
      )
      .unit

  override def getRecords(streamName: String): Task[List[String]] = {
    for {
      // For simplicity, we assume 1 shard and get the first one's iterator
      shardsResp <- ZIO.fromCompletableFuture(
        client.listShards(r => r.streamName(streamName))
      )
      shardId = shardsResp.shards().get(0).shardId()

      iteratorResp <- ZIO.fromCompletableFuture(
        client.getShardIterator(
          GetShardIteratorRequest
            .builder()
            .streamName(streamName)
            .shardId(shardId)
            .shardIteratorType(ShardIteratorType.TRIM_HORIZON)
            .build()
        )
      )
      iterator = iteratorResp.shardIterator()

      recordsResp <- ZIO.fromCompletableFuture(
        client.getRecords(
          GetRecordsRequest.builder().shardIterator(iterator).build()
        )
      )
    } yield recordsResp.records().asScala.toList.map(_.data().asUtf8String())
  }
}

object KinesisServiceImpl {
  val layer: ZLayer[KinesisAsyncClient, Nothing, KinesisService] =
    ZLayer.fromFunction(KinesisServiceImpl(_))
}
