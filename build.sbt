import com.typesafe.sbt.packager.docker.{ Cmd, ExecCmd }

///////////////////////////////////////////////////////////////////////////////////////////////////
// Settings
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val buildSettings = Seq(
  organization := "com.github.paulroseau",
  scalaVersion := "2.11.12"
)

lazy val commonSettings = Seq(
  name := "docker-spark-demo",
  version := "latest",
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  scalacOptions ++= commonScalacOptions,
  fork in Test := true,
  parallelExecution in Test := false,
  scalacOptions in (Compile, doc) := (scalacOptions in (Compile, doc)).value
    .filter(_ != "-Xfatal-warnings")
) ++ warnUnusedImport ++ partialUnification

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlog-reflective-calls",
  "-Ywarn-inaccessible",
  "-Ypatmat-exhaust-depth",
  "20",
  "-Ydelambdafy:method",
  "-Xmax-classfile-name",
  "100"
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) ⇒
        Seq()
      case Some((2, n)) if n >= 11 ⇒
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Seq("-Xlint", "-Ywarn-unused-import").contains)
  },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val partialUnification = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 ⇒
        Seq("-Ypartial-unification")
      case _ ⇒
        Seq()
    }
  }
)

lazy val credentialSettings = Seq(
  credentials ++= {
    if ((Path.userHome / ".ivy2" / ".credentials").exists())
      Seq(Credentials(Path.userHome / ".ivy2" / ".credentials"))
    else
      Seq.empty
  },
  coursierUseSbtCredentials := true
)

lazy val formatSettings = Seq(
  scalafmtOnCompile := true
)

lazy val resolverSettings = Seq(
  resolvers ++= Seq(
    "Typesafe Releases".at("http://repo.typesafe.com/typesafe/maven-releases/")
  )
)

lazy val allSettings =
  buildSettings ++
    commonSettings ++
    credentialSettings ++
    formatSettings ++
    resolverSettings

///////////////////////////////////////////////////////////////////////////////////////////////////
// Dependencies
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val D = new {
  val Versions = new {
    val spark = "2.2.1"
  }
  val spark = "org.apache.spark" %% "spark-core" % Versions.spark % "provided"
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Projects
///////////////////////////////////////////////////////////////////////////////////////////////////

lazy val `docker-spark-demo` =
  Project(
    id = "docker-spark-demo",
    base = file(".")
  ).settings(moduleName := "root")
    .settings(allSettings)
    .aggregate(jobs)

lazy val jobs =
  Project(
    id = "jobs",
    base = file("jobs")
  ).enablePlugins(DockerPlugin)
    .settings(moduleName := "jobs")
    .settings(allSettings)
    .settings(
      Seq(
        dockerBaseImage := "test/spark-submit",
        packageName in Docker := name.value,
        version in Docker := version.value,
        mappings in Universal := Seq(
          (assembly in Compile map { jar ⇒
            jar → s"docker-spark-demo-assembly.jar"
          }).value
        ),
        defaultLinuxInstallLocation in Docker := "/opt/jars",
        dockerCommands in Docker := (dockerCommands in Docker).value filterNot {
          case ExecCmd("ENTRYPOINT", _) ⇒ true
          case Cmd("ENTRYPOINT", _)     ⇒ true
          case _                        ⇒ false
        }
      )
    )
    .settings(
      libraryDependencies += D.spark
    )
