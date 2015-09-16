enablePlugins(ScalaJSPlugin)
scalaJSStage in Global := FastOptStage

lazy val root = project.
  in(file(".")).
  aggregate(miniJS, miniJVM)

lazy val mini = crossProject.
  crossType(CrossType.Pure).
  in(file(".")).
  settings(
    name := "hyperflux-mini-app",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies += "hyperflux" %%% "hyperflux-framework" % "0.1",
    autoCompilerPlugins := true,
    addCompilerPlugin("hyperflux" %% "hyperflux-plugin" % "0.1")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.10.0",
      "org.http4s" %% "http4s-blaze-server" % "0.10.0"
    )
  )

lazy val miniJS = mini.js
lazy val miniJVM = mini.jvm
