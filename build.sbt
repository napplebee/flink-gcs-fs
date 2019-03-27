
name := "flink-gcs-fs"

organization := "binx.io"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.12"

val flinkVersion = "1.7.2"
val hadoopVersion = "2.9.2"

// provided
libraryDependencies += "com.google.cloud.bigdataoss" % "gcs-connector" % "1.9.4-hadoop3" % Provided // gcs-connector is provided on GCP - DataProc
libraryDependencies += "org.apache.flink" % "flink-core" % flinkVersion % Provided
libraryDependencies += "org.apache.flink" % "flink-hadoop-fs" % flinkVersion % Provided
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Provided
libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Provided

// test scope
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.7" % Test
libraryDependencies += "org.apache.flink" %% "flink-scala" % flinkVersion % Test
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % Test

// testing configuration
fork in Test := true
parallelExecution := false

// disable using the Scala version in output paths and artifacts
crossPaths := false

// set the license
licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// set environment var for test
// Flink will look for the “core-site.xml” and “hdfs-site.xml” files in the specified directory.
envVars in Test := Map(
  "HADOOP_CONF_DIR" -> "/Users/dennis/projects/flink-gcs-fs/src/test/resources",
  "HADOOP_HOME" -> "/tmp"
)