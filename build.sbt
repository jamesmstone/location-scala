lazy val pekkoHttpVersion = "1.0.1"
lazy val pekkoVersion     = "1.0.3"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "one.jamesst",
      scalaVersion    := "3.3.3"
    )),
    name := "Location",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http"                % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-http-spray-json"     % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-actor-typed"         % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream"              % pekkoVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.13",

      "org.apache.pekko" %% "pekko-http-testkit"        % pekkoHttpVersion % Test,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.19"         % Test,

      "org.scalikejdbc" %% "scalikejdbc"  % "4.3.0",
      "org.scalikejdbc" %% "scalikejdbc-config"  % "4.3.1",
      "org.xerial"         % "sqlite-jdbc"          % "3.46.1.0",
      "ch.qos.logback"  %  "logback-classic"   % "1.5.6",
    )
  )

// avoid generating docs
Compile / doc / sources                := Nil
Compile / packageDoc / publishArtifact := false

enablePlugins(JavaAppPackaging)
