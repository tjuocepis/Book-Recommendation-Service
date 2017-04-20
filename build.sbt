name := "edgaras_juocepis_final_project_cs441"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
//  // jackson
  "org.json4s" %% "json4s-native" % "3.5.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
//  // akka
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.2",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.11",
  "com.typesafe.akka" %% "akka-remote" % "2.4.11",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",
//  // google
  "com.google.api-client" % "google-api-client" % "1.22.0" exclude("com.google.guava", "guava-jdk5"),
//  // mysql
  "com.google.cloud.sql" % "mysql-socket-factory-parent" % "1.0.2",
  "com.google.cloud.sql" % "mysql-socket-factory-core" % "1.0.2",
  "com.google.cloud.sql" % "mysql-socket-factory-connector-j-6" % "1.0.2",
  // logger
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.6.2",
  // elasticsearch
  "org.elasticsearch" % "elasticsearch" % "2.4.0"
)