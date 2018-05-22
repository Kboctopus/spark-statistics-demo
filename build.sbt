name := "spark-statistics-demo"

version := "1.0"

scalaVersion := "2.10.6"

compileOrder in ThisBuild := CompileOrder.JavaThenScala

unmanagedBase := baseDirectory.value / "lib"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
  "org.apache.spark" %% "spark-hive" % "1.6.0",
  "org.apache.spark" %% "spark-streaming" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0",

  "org.elasticsearch" %% "elasticsearch-spark" % "2.3.0",
  "org.apache.phoenix" % "phoenix-spark" % "4.4.0-HBase-1.0"
)