package binx

import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem

class WriteToGCS extends TestSpec {
  it should "do a wordcount" in withDataSetEnv { env =>
    // source
    val text: DataSet[String] = env.fromCollection(TestSpec.Text)

    // transformations
    val counts: AggregateDataSet[(String, Int)] = {
      text
        .flatMap(_.toLowerCase.split("\\W+"))
        .map((_, 1))
        .groupBy(0)
        .sum(1)
      }

    counts.writeAsCsv("gs://flink-gcs-fs-test/flink-write-as-text-test.csv", writeMode = FileSystem.WriteMode.OVERWRITE)
    env.execute("WordCount s3 csv")
  }
}
