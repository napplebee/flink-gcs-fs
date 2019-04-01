package binx

import java.io.OutputStream
import java.io.PrintStream

import org.apache.flink.streaming.api.scala._
import binx.generator.DataGenerator
import org.apache.flink.api.common.serialization.{Encoder, SimpleStringEncoder}
import org.apache.flink.api.common.time.Time
import org.apache.flink.core.fs.Path
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import java.util.concurrent.TimeUnit

class WriteToGCS extends TestSpec {
  it should "do a wordcount" in withDataStreamEnv { env =>
//    val config = env.getCheckpointConfig
//    config.setCheckpointInterval(1000)
//    config.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
//    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(10L, TimeUnit.SECONDS)))
//    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
//    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.setStateBackend(new FsStateBackend("gs://flink-gcs-fs-state/flink.state"))

    // start a checkpoint every 5 seconds
    env.setMaxParallelism(1)
    env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE, force = true)

    // source
    val stream = DataGenerator.sequentialLong(env, 10000)
    val strings = stream.map(nr => s"$nr\n")

    val sink: StreamingFileSink[String] = StreamingFileSink
      .forRowFormat(new Path("gs://flink-gcs-fs-test/flink-write-as-text-test.csv"), new Encoder[String] {
        override def encode(element: String, stream: OutputStream): Unit = {
          println("encoding element: " + element)
          stream.write(element.getBytes("UTF-8"))
        }
      })
      .build()

    strings.addSink(sink)

    env.execute("tester")
  }
}
