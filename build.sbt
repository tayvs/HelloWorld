organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

//lazy val `helloworld` = (project in file("."))
//  .aggregate(`helloworld-api`, `helloworld-impl`, `helloworld-stream-api`, `helloworld-stream-impl`)
//
//lazy val `helloworld-api` = (project in file("helloworld-api"))
//  .settings(
//    libraryDependencies ++= Seq(
//      lagomScaladslApi
//    )
//  )
//
//lazy val `helloworld-impl` = (project in file("helloworld-impl"))
//  .enablePlugins(LagomScala)
//  .settings(
//    libraryDependencies ++= Seq(
//      lagomScaladslPersistenceCassandra,
//      lagomScaladslKafkaBroker,
//      lagomScaladslTestKit,
//      macwire,
//      scalaTest
//    )
//  )
//  .settings(lagomForkedTestSettings: _*)
//  .dependsOn(`helloworld-api`)
//
//lazy val `helloworld-stream-api` = (project in file("helloworld-stream-api"))
//  .settings(
//    libraryDependencies ++= Seq(
//      lagomScaladslApi
//    )
//  )
//
//lazy val `helloworld-stream-impl` = (project in file("helloworld-stream-impl"))
//  .enablePlugins(LagomScala)
//  .settings(
//    libraryDependencies ++= Seq(
//      lagomScaladslTestKit,
//      macwire,
//      scalaTest
//    )
//  )
//  .dependsOn(`helloworld-stream-api`, `helloworld-api`)
//

lagomKafkaEnabled in ThisBuild := false

lazy val `statistic` = (project in file("."))
  .aggregate(`statistic-api`, `statistic-impl`, `acct-kafka-topic`)

lazy val `statistic-api` = (project in file("statistic-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`acct-kafka-topic`)

lazy val `acct-kafka-topic` = (project in file("acct-kafka-topic"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `statistic-impl` = (project in file("statistic-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`statistic-api`, `acct-kafka-topic`)
