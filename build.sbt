import play.sbt.PlayImport.PlayKeys
import sbt.CrossVersion

val libName         = "play-frontend-govuk"

lazy val root = Project(libName, file("."))
  .enablePlugins(PlayScala, SbtTwirl, BuildInfoPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .configs(IntegrationTest)
  .settings(
    name := libName,
    majorVersion := 1,
    scalaVersion := "2.12.13",
    libraryDependencies ++= LibDependencies(),
    PlayCrossCompilation.playCrossCompilationSettings,
    isPublicArtefact := true,
    parallelExecution in sbt.Test := false,
    PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value,
    unmanagedResourceDirectories in Test ++= Seq(baseDirectory(_ / "target/web/public/test").value),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "playVersion"          -> PlayCrossCompilation.playVersion,
      sources in (Compile, TwirlKeys.compileTemplates)
    )
  )
