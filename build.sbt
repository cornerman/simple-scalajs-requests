inThisBuild(Seq(
  version := "0.1.0-SNAPSHOT",

  organization := "com.github.cornerman",

  scalaVersion := "2.12.15",
  crossScalaVersions := Seq("2.12.15", "2.13.8", "3.1.1"),
))

lazy val commonSettings = Seq(
)

lazy val jsSettings = Seq(
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq.empty //TODO?
    case _ =>
      val githubRepo    = "cornerman/simple-scalajs-requests"
    val local = baseDirectory.value.toURI
      val subProjectDir = baseDirectory.value.getName
      val remote        = s"https://raw.githubusercontent.com/${githubRepo}/${git.gitHeadCommit.value.get}"
      Seq(s"-P:scalajs:mapSourceURI:$local->$remote/${subProjectDir}/")
  })
)

lazy val requests = project
  .enablePlugins(ScalaJSPlugin)
  .in(file("requests"))
  .settings(commonSettings, jsSettings)
  .settings(
    name := "requests",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.1.0"
    )
  )

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(requests)
