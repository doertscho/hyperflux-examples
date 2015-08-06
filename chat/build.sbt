lazy val root = (project in file(".")).
  settings(
    name := "hyperflux-chat",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.8.1",
      "org.scala-lang.modules" %% "scala-async" % "0.9.3",
      "com.lihaoyi" %% "upickle_sjs0.6" % "0.3.4",
      "hyperflux" %% "hyperflux-framework" % "0.1"
    )
  )

enablePlugins(ScalaJSPlugin)
scalaJSStage in Global := FastOptStage

autoCompilerPlugins := true
addCompilerPlugin("hyperflux" %% "hyperflux-plugin" % "0.1")

scalacOptions ++= Seq(
//  "-Xshow-phases",
//  "-Ybrowse:parser",
//  "-Ybrowse:packageobjects",
//  "-Ybrowse:namer",
//  "-Ybrowse:typer",
//  "-Ylog:typer",
//  "-Ybrowse:hf-i-analyzer",
//  "-Ybrowse:hf-u-analyzer",
  "-Ybrowse:hf-proxifier",
//  "-Yshow-syms",
  "-Xdev",
  "-Ydebug"
)
