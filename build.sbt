organization  := "br.tioback.urlshortener"

version       := "0.1"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"               %%  "spray-can"          % sprayV,
    "io.spray"               %%  "spray-json"         % "1.3.2",
    "io.spray"               %%  "spray-routing"      % sprayV,
    "io.spray"               %%  "spray-testkit"      % sprayV  % "test",
    "com.typesafe.akka"      %%  "akka-actor"         % akkaV,
    "com.typesafe.akka"      %%  "akka-testkit"       % akkaV   % "test",
    "org.specs2"             %%  "specs2-core"        % "2.3.11" % "test",
	"com.sksamuel.elastic4s" %%  "elastic4s-core"     % "1.7.4",
	"org.codehaus.groovy"    %   "groovy-all"         % "2.4.5"
  )
}

Revolver.settings
