val scala3Version = "3.0.0-M3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "api-tlist-diagnose",
    version := "1.0.1",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "com.github.scopt" %% "scopt" % "4.0.0"
    ) 
  )
