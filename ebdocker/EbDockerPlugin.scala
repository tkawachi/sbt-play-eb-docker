package com.github.tkawachi

import java.nio.charset.Charset

import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{Docker, dockerExposedPorts}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{stage, stagingDirectory}
import play.sbt.Play
import play.sbt.PlayImport.PlayKeys
import sbt._, Keys._

object EbDockerPlugin extends AutoPlugin {

  object autoImport {
    val ebDocker = taskKey[File]("Package for Elastic beanstalk with Docker")
    val ebDockerContainerPort = settingKey[Int]("Container port for Elastic beanstalk with Docker")
    val ebDockerrunVersion = settingKey[Int]("AWSEBDockerrunVersion in Dockerrun.aws.json")
    val ebDockerOutputFile = settingKey[File]("Output file of Elastic beanstalk with Docker")

    val ebDockerDefaultSettings = Seq(
      ebDockerContainerPort := PlayKeys.playDefaultPort.value,
      dockerExposedPorts := (dockerExposedPorts.value :+ ebDockerContainerPort.value).distinct,
      ebDockerOutputFile := target.value / (name.value + "-" + version.value + ".zip"),
      ebDocker := {
        // run docker:stage
        (stage in Docker).value

        val stageDir = (stagingDirectory in Docker).value

        // Write Dockerrun.aws.json
        writeDockerrunAwsJson(ebDockerrunVersion.value, ebDockerContainerPort.value, stageDir)

        // Create a zip file
        // Using an external zip command.
        val zipFile: File = ebDockerOutputFile.value
        val zipContents: Array[String] = stageDir.listFiles().flatMap(_.relativeTo(stageDir)).map(_.toString)
        Process(Seq("zip", "-q", "-r", zipFile.absolutePath) ++ zipContents, stageDir) ! streams.value.log

        val printPath = zipFile.relativeTo(baseDirectory.value).getOrElse(zipFile.absolutePath)
        streams.value.log.info(s"Upload $printPath to Elastic beanstalk")
        zipFile
      },
      ebDockerrunVersion := 1
    )
  }

  import autoImport._

  override def requires = Play && DockerPlugin && UniversalPlugin

  override def trigger = allRequirements

  override lazy val projectSettings = ebDockerDefaultSettings

  private def writeDockerrunAwsJson(ebDockerrunVersion: Int, ebContainerPort: Int, stagingDir: File): Unit = {
      val json =
        s"""{
          |   "AWSEBDockerrunVersion": "$ebDockerrunVersion",
          |   "Ports": [{
          |       "ContainerPort": "$ebContainerPort"
          |   }]
          |}
          |""".stripMargin

      val dockerrun: File = new File(stagingDir, "Dockerrun.aws.json")
      IO.write(dockerrun, json, Charset.forName("UTF-8"))
  }
}
