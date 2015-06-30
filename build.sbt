val commonSettings = Seq(
  organization := "com.github.tkawachi",
  scalaVersion := "2.10.5"
)

lazy val root = project.in(file("."))
  .settings(commonSettings ++ Seq(
    name := "sbt-play-eb-docker",
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.1"),
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % "1.10.2",
    sbtPlugin := true
  ): _*)
