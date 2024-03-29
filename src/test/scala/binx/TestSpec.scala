package binx

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.{Configuration, GlobalConfiguration}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.slf4j.{Logger, LoggerFactory}

object TestSpec {
  val Text: List[String] = List(
    "To be, or not to be,--that is the question:--",
    "Whether 'tis nobler in the mind to suffer",
    "The slings and arrows of outrageous fortune",
    "Or to take arms against a sea of troubles,"
  )
}

abstract class TestSpec extends FlatSpec with Matchers {
  val log: Logger = LoggerFactory.getLogger(getClass)

  /**
    * Returns a random UUID type 4 String
    * @return String
    */
  def randomId(): String = {
    UUID.randomUUID().toString()
  }

  def loadConfiguration(): Configuration = {
    GlobalConfiguration.loadConfiguration()
  }

  /**
    * Flink [DataSet API](https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/batch/index.html)
    * for bounded data sets
    */
  def withDataSetEnv(f: ExecutionEnvironment => Unit): Unit = {
    log.info("Testing withDataSetEnv")
    val env = ExecutionEnvironment.createLocalEnvironment(loadConfiguration())
    f(env)
  }

  /**
    * Flink [DataStream API](https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/datastream_api.html#flink-datastream-api-programming-guide),
    * for bounded or unbounded streams of data
    */
  def withDataStreamEnv(f: StreamExecutionEnvironment => Unit): Unit = {
    log.info("Testing withDataStreamEnv, num-cpu={}", StreamExecutionEnvironment.getDefaultLocalParallelism)
    val env = StreamExecutionEnvironment.createLocalEnvironment(1, loadConfiguration())
    f(env)
  }
}