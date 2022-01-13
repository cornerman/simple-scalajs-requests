inThisBuild(Seq(
  version := "0.1.0-SNAPSHOT",

  organization := "com.github.cornerman",

  scalaVersion := "2.12.15",

  crossScalaVersions := Seq("2.12.15", "2.13.8"),
))

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
)

lazy val jsSettings = Seq(
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
      "org.scala-js" %%% "scalajs-dom" % "2.0.0"
    )
  )

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(requests)
