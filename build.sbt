lazy val commonSettings = Seq(
  version       := "0.1.0",
  scalaVersion  := "2.11.8"
)

lazy val `scala-bft` = project.in(file("."))
  .aggregate(`scala-bft-replica`)


lazy val `scala-bft-replica` = project.in(file("bft-replica"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.bftReplica,

    parallelExecution in Test := false
  )
