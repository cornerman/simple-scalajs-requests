inThisBuild(Seq(
  organization := "com.github.cornerman",

  scalaVersion := "2.12.17",
  crossScalaVersions := Seq("2.12.17", "2.13.12", "3.2.0"),

  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/MIT")),

  homepage := Some(url("https://github.com/cornerman/simple-scalajs-requests")),

  scmInfo := Some(ScmInfo(
    url("https://github.com/cornerman/simple-scalajs-requests"),
    "scm:git:git@github.com:cornerman/simple-scalajs-requests.git",
    Some("scm:git:git@github.com:cornerman/simple-scalajs-requests.git"))
  ),

  pomExtra :=
    <developers>
      <developer>
        <id>jkaroff</id>
        <name>Johannes Karoff</name>
        <url>https://github.com/cornerman</url>
      </developer>
    </developers>
))

lazy val commonSettings = Seq(
)

lazy val jsSettings = Seq(
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq.empty //TODO?
    case _ =>
      val githubRepo    = "cornerman/simple-scalajs-requests"
      val local         = baseDirectory.value.toURI
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
    name := "simple-scalajs-requests",

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.3.0"
    )
  )
