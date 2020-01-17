import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import scala.collection.mutable
val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.31")
def scala213 = "2.13.1"
def scala212 = "2.12.10"
def scala211 = "2.11.12"
def dotty = "0.21.0-RC1"
inThisBuild(
  List(
    organization := "org.scalameta",
    homepage := Some(url("https://github.com/scalameta/munit")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "olafurpg",
        "Ólafur Páll Geirsson",
        "olafurpg@gmail.com",
        url("https://geirsson.com")
      )
    ),
    scalaVersion := scala213,
    testFrameworks := List(
      new TestFramework("munit.Framework")
    ),
    resolvers += Resolver.sonatypeRepo("public"),
    useSuperShell := false,
    scalacOptions ++= List(
      "-language:implicitConversions"
    )
  )
)

skip in publish := true
crossScalaVersions := List()
val isPreScala213 = Set("2.11", "2.12")
val scala2Versions = List(scala211, scala212, scala213)
val scalaVersions = scala2Versions ++ List(dotty)
def isScala2(binaryVersion: String): Boolean = binaryVersion.startsWith("2")
def isScala3(binaryVersion: String): Boolean = binaryVersion.startsWith("0")
val sharedJSSettings = List(
  crossScalaVersions := scala2Versions
)

lazy val munit = crossProject(JSPlatform, JVMPlatform)
  .settings(
    crossScalaVersions := List(scala213, scala212, scala211, dotty),
    unmanagedSourceDirectories.in(Compile) ++= {
      val base =
        baseDirectory.in(ThisBuild).value / "munit" / "shared" / "src" / "main"
      val result = mutable.ListBuffer.empty[File]
      val binaryVersion = scalaBinaryVersion.value
      if (isPreScala213(binaryVersion)) {
        result += base / "scala-pre-2.13"
      }
      if (isScala2(binaryVersion)) {
        result += base / "scala-2"
      }
      result.toList
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.11" =>
          List(
            "-Xexperimental",
            "-Ywarn-unused-import"
          )
        case "0.21" => List()
        case _ =>
          List(
            "-target:jvm-1.8",
            "-Yrangepos"
            // -Xlint is unusable because of
            // https://github.com/scala/bug/issues/10448
            // "-Ywarn-unused:imports"
          )
      }
    },
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "0.21" => Nil
        case _ =>
          List("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      }
    }
  )
  .jsSettings(
    sharedJSSettings,
    libraryDependencies ++= List(
      "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion,
      "org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= List(
      "junit" % "junit" % "4.13",
      "com.geirsson" % "junit-interface" % "0.11.6",
      "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
    )
  )
lazy val munitJVM = munit.jvm
lazy val munitJS = munit.js

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .dependsOn(munit)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "munit",
    buildInfoKeys := Seq[BuildInfoKey](
      "sourceDirectory" ->
        baseDirectory.in(ThisBuild).value / "tests" / "shared" / "src" / "main",
      scalaVersion
    ),
    skip in publish := true
  )
  .jsSettings(sharedJSSettings)
  .jvmSettings(
    crossScalaVersions := scalaVersions,
    fork := true
  )

lazy val docs = project
  .in(file("munit-docs"))
  .dependsOn(munitJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .settings(
    crossScalaVersions := List(scala213),
    mdocOut :=
      baseDirectory.in(ThisBuild).value / "website" / "target" / "docs",
    mdocVariables := Map(
      "VERSION" -> version.value.replaceFirst("\\+.*", "")
    ),
    fork := false
  )
