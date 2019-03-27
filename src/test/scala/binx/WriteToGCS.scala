package binx

import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.time.Time

class WriteToGCS extends TestSpec {
  it should "do a wordcount" in withDataStreamEnv { env =>
//    val config = env.getCheckpointConfig
//    config.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
//    env.setStateBackend(new FsStateBackend("gs://flink-gcs-fs-state/flink.state"))

    // source
    val text = env.fromCollection(TestSpec.Text)

    // transformations
    val counts = text
      .flatMap(_.toLowerCase.split("\\W+"))
      .map((_, 1))
      .keyBy(0)
      .timeWindow(Time.seconds(5))
      .sum(1)

    counts.writeAsCsv("gs://flink-gcs-fs-test/flink-write-as-text-test.csv", writeMode = FileSystem.WriteMode.OVERWRITE)
    env.execute("WordCount gcs csv")
  }
}
