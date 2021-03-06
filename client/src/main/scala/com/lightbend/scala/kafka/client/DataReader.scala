package com.lightbend.scala.kafka.client

import com.lightbend.java.configuration.kafka.ApplicationKafkaParameters._
import com.lightbend.scala.kafka.MessageListener

object DataReader {

  def main(args: Array[String]) {

    println(s"Using kafka brokers at ${KAFKA_BROKER}")

    val listener = MessageListener(KAFKA_BROKER, DATA_TOPIC, DATA_GROUP, new RecordProcessor())
    listener.start()
  }
}
