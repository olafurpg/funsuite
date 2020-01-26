package munit

import sbt._
import sbt.Keys._
import sbt.plugins._
import java.nio.file.Paths
import java.nio.file.Files
import java.{util => ju}

object MUnitPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = JvmPlugin
  object autoImport {
    val munitRepository: SettingKey[Option[String]] =
      settingKey[Option[String]](
        "The repository of this project, for example GitHub URL."
      )
    val munitRunId: SettingKey[Option[String]] =
      settingKey[Option[String]]("Unique identifier for this test run.")
    val munitReportListener: SettingKey[Option[MUnitReportListener]] =
      settingKey[Option[MUnitReportListener]](
        "The listener to handle reports."
      )
  }
  import autoImport._

  override val globalSettings: List[Setting[_ <: Option[Object]]] = List(
    munitReportListener := {
      for {
        credentials <- Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
        path = Paths.get(credentials).toAbsolutePath()
        if Files.exists(path) || {
          Option(System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON")) match {
            case Some(base64) =>
              val json = ju.Base64.getDecoder().decode(base64)
              Files.createDirectories(path.getParent())
              Files.write(path, json)
              true
            case None =>
              false
          }
        }
      } yield new MUnitGcpListener()
    },
    munitRepository := Option(System.getenv("GITHUB_REPOSITORY")),
    munitRunId := Option(System.getenv("GITHUB_ACTION"))
  )

  override val projectSettings: Seq[Def.Setting[_]] = List(
    testListeners ++= {
      for {
        runId <- munitRunId.value.toList
        repository <- munitRepository.value.toList
        listener <- munitReportListener.value.toList
      } yield new MUnitTestsListener(
        listener,
        repository,
        runId,
        scalaVersion.value,
        thisProject.value.id
      )
    }
  )
}
