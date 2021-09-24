val libName         = "play-frontend-govuk"

lazy val root = Project(libName, file("."))
  .enablePlugins(PlayScala, SbtTwirl, BuildInfoPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .configs(IntegrationTest)
  .settings(
    name := libName,
    majorVersion := 1,
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(),
    PlayCrossCompilation.playCrossCompilationSettings,
    isPublicArtefact := true,
    parallelExecution in sbt.Test := false,
    buildInfoKeys ++= Seq[BuildInfoKey](
      "playVersion"          -> PlayCrossCompilation.playVersion,
      sources in (Compile, TwirlKeys.compileTemplates)
    )
  )
