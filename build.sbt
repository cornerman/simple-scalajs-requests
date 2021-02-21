import Options._

inThisBuild(Seq(
  version := "0.1.0-SNAPSHOT",

  organization := "com.github.cornerman",

  scalaVersion := "2.12.11",

  crossScalaVersions := Seq("2.12.11", "2.13.2"),
))

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),

  scalacOptions ++= CrossVersion.partialVersion(scalaVersion.value).toList.flatMap { case (major, minor) =>
    versionBasedOptions(s"${major}.${minor}")
  },
  scalacOptions in (Compile, console) ~= (_.diff(badConsoleFlags)),
)

lazy val jsSettings = Seq(
  scalacOptions ++= scalajsOptions,

  scalacOptions += {
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/cornerman/simple-scalajs-requests/${git.gitHeadCommit.value.get}/"
    s"-P:scalajs:mapSourceURI:$local->$remote"
  }
)

lazy val requests = project
  .enablePlugins(ScalaJSPlugin)
  .in(file("requests"))
  .settings(commonSettings, jsSettings)
  .settings(
    name := "requests",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0"
    )
  )

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(requests)
