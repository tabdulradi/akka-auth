name := "akka-auth"

version := "0.0.1"

scalaVersion := "2.11.2"

def akka(p: String) = "com.typesafe.akka" %% s"akka-$p" % "2.3.6"
def play(p: String) = "com.typesafe.play" %% s"play-$p" % "2.4.0-M1"
def scala(m: String) = "org.scala-lang.modules" %% s"scala-$m" % "1.0.2"

libraryDependencies ++= Seq(
  akka("actor"),
  akka("testkit") % "test",
  play("json"),
  play("ws"),
  scala("parser-combinators")
)
