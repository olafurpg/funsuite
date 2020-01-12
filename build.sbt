def scala213 = "2.13.1"
def scala212 = "2.12.10"
def scala211 = "2.11.12"
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
    crossScalaVersions := List(scala213, scala212, scala211),
    fork := true,
    testFrameworks := List(
      new TestFramework("munit.Framework")
    ),
    resolvers += Resolver.sonatypeRepo("public"),
    useSuperShell := false
  )
)

skip in publish := true

lazy val fansiVersion = Def.setting[String] {
  if (scalaVersion.value.startsWith("2.11")) "0.2.6"
  else "0.2.7"
}
lazy val pprintVersion = Def.setting[String] {
  if (scalaVersion.value.startsWith("2.11")) "0.5.4"
  else "0.5.7"
}

lazy val munit = project
  .settings(
    unmanagedSourceDirectories.in(Compile) ++= {
      scalaBinaryVersion.value match {
        case "2.12" | "2.11" =>
          List(sourceDirectory.in(Compile).value / "scala-pre-2.13")
        case _ =>
          Nil
      }
    },
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.11" =>
          List(
            "-Xexperimental",
            "-Ywarn-unused-import"
          )
        case _ =>
          List(
            "-target:jvm-1.8",
            "-Yrangepos",
            // -Xlint is unusable because of
            // https://github.com/scala/bug/issues/10448
            "-Ywarn-unused:imports"
          )
      }
    },
    libraryDependencies ++= List(
      "junit" % "junit" % "4.13",
      "com.geirsson" % "junit-interface" % "0.11.6",
      "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
    )
  )

lazy val tests = project
  .dependsOn(munit)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "munit",
    buildInfoKeys := Seq[BuildInfoKey](
      "sourceDirectory" -> sourceDirectory.in(Compile).value
    ),
    skip in publish := true
  )

lazy val docs = project
  .in(file("munit-docs"))
  .dependsOn(munit)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .settings(
    mdocOut :=
      baseDirectory.in(ThisBuild).value / "website" / "target" / "docs",
    mdocVariables := Map(
      "VERSION" -> version.value.replaceFirst("\\+.*", "")
    ),
    fork := false
  )
