lazy val root = (project in file(".")).
  settings(
    name := "hyperflux-chat",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.8.1",
      "org.scala-lang.modules" %% "scala-async" % "0.9.3",
      "hyperflux" %% "hyperflux-framework" % "0.1"
    )
  )

enablePlugins(ScalaJSPlugin)
scalaJSStage in Global := FastOptStage

autoCompilerPlugins := true
resolvers += "local maven repository" at
  "file:/share/dev/lib/maven"
addCompilerPlugin("hyperflux" %% "hyperflux-plugin" % "0.1")

scalacOptions +=
//  "-Xshow-phases"
//  "-Ybrowse:typer"
  "-Ybrowse:hf-analyzer"
