val commonSettings = Seq(
  organization := "com.github.tkawachi",
  scalaVersion := "2.10.5"
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(ebdocker)

lazy val ebdocker = project
  .settings(commonSettings ++ Seq(
    name := "sbt-ebdocker",
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.1"),
    sbtPlugin := true
  ): _*)
