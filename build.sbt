lazy val commonSettings = Seq(
  version       := "0.1.0",
  scalaVersion  := "2.11.8"
)

lazy val `scala-bft` = project.in(file("."))
  .aggregate(bft)


lazy val bft = project.in(file("bft"))
  .settings(commonSettings: _*)
  .settings(
   libraryDependencies ++= Dependencies.bft
  )
