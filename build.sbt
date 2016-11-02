name := "codemesh-akka-workshop"

val projectVersion        = "0.1-SNAPSHOT"
val akkaVersion           = "2.4.12"
val scalacticVersion      = "3.0.0"
val scalatestVersion      = "3.0.0"
val logbackVersion        = "1.1.3"
val parCombVersion        = "1.0.2"


lazy val root = (project in file(".")).
  settings(
    version := projectVersion,
    scalaVersion := "2.11.8",
    retrieveManaged := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scala-lang.modules" %% "scala-parser-combinators" % parCombVersion,
      "org.scalactic" %% "scalactic" % scalacticVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
    ),
    scalacOptions := Seq(
      "-encoding",
      "UTF-8",
      "-target:jvm-1.8",
      "-deprecation",
      "-language:_"
    ),
    fork in (Test, run) := true
  )


