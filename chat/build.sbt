enablePlugins(ScalaJSPlugin)
scalaJSStage in Global := FastOptStage

lazy val root = project.
  in(file(".")).
  aggregate(chatJS, chatJVM)

lazy val chat = crossProject.
  crossType(CrossType.Pure).
  in(file(".")).
  settings(
    name := "hyperflux-chat",
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
  
lazy val chatJS = chat.js
lazy val chatJVM = chat.jvm
