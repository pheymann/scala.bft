import sbt._

trait Versions {

  val specs2Verison = "3.8.4"

}

trait Libraries extends Versions {

  val specs2 = "org.specs2" %% "specs2-core" % specs2Verison

}

object Dependencies extends Libraries {

  val bft = Seq(
    specs2 % Test
  )

}
