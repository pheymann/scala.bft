import sbt._

trait Versions {

  val akkaVersion   = "2.4.8"

  val specs2Verison = "3.8.4"

}

trait Libraries extends Versions {

  val akka    = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val specs2  = "org.specs2" %% "specs2-core" % specs2Verison

}

object Dependencies extends Libraries {

  val bft = Seq(
    akka    % Compile,
    specs2  % Test
  )

}
