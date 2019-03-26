
name := "flink-gcs-fs"

organization := "binx.io"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.12"

val flinkVersion = "1.7.2"
val hadoopVersion = "2.9.2"

// compile
libraryDependencies += "com.google.cloud.bigdataoss" % "gcs-connector" % "1.9.4-hadoop3"

// provided
libraryDependencies += "org.apache.flink" % "flink-core" % flinkVersion % Provided
libraryDependencies += "org.apache.flink" % "flink-hadoop-fs" % flinkVersion % Provided
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Provided

// test scope
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

// testing configuration
fork in Test := true
parallelExecution := false

// disable using the Scala version in output paths and artifacts
crossPaths := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))