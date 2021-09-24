import play.sbt.PlayImport.PlayKeys
import sbt.CrossVersion

val libName         = "play-frontend-govuk"
val silencerVersion = "1.7.2"

lazy val IntegrationTest = config("it") extend Test

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
    // ***************
    // Use the silencer plugin to suppress warnings from unused imports in compiled twirl templates
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    // ***************
    (generateUnitTestFixtures in Test) := {
      val generateFixtures = GenerateFixtures(baseDirectory.value / "src/test/resources")
      generateFixtures.generate()
    },
    parallelExecution in sbt.Test := false,
    PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value,
    unmanagedResourceDirectories in Test ++= Seq(baseDirectory(_ / "target/web/public/test").value),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "playVersion"          -> PlayCrossCompilation.playVersion,
      "govukFrontendVersion" -> LibDependencies.govukFrontendVersion,
      sources in (Compile, TwirlKeys.compileTemplates)
    )
  )
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings))

lazy val generateUnitTestFixtures = taskKey[Unit]("Generate unit test fixtures")
