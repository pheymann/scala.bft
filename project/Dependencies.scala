import sbt._

trait Versions {

  val akkaVersion = "2.4.8"

  val specs2Verison   = "3.8.4"
  val logbackVersion  = "1.1.7"

}

trait Libraries extends Versions {

  val akka        = "com.typesafe.akka" %% "akka-actor"   % akkaVersion
  val akkaSlf4j   = "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion
  val akkaRemote  = "com.typesafe.akka" %% "akka-remote"  % akkaVersion
  val akkaStream  = "com.typesafe.akka" %% "akka-stream"  % akkaVersion
  val akkaTest    = "com.typesafe.akka" %% "akka-testkit" % akkaVersion

  val specs2  = "org.specs2"      %% "specs2-core"    % specs2Verison
  val logback = "ch.qos.logback"  % "logback-classic" % logbackVersion

}

object Dependencies extends Libraries {

  val bftReplica = Seq(
    akka        % Compile,
    akkaSlf4j   % Compile,
    akkaRemote  % Compile,
    akkaStream  % Compile,

    akkaTest  % Test,
    specs2    % Test,
    logback   % Test
  )

}
