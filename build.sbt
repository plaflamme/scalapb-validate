import Settings.stdSettings

val Scala213 = "2.13.2"

val Scala212 = "2.12.10"

skip in publish := true

sonatypeProfileName := "com.thesamet"

inThisBuild(
  List(
    organization := "com.thesamet.scalapb",
    homepage := Some(url("https://github.com/scalapb/scalapb-validate")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "thesamet",
        "Nadav Samet",
        "thesamet@gmail.com",
        url("https://www.thesamet.com")
      )
    )
  )
)

val pgvVersion = "0.3.0"

lazy val core = projectMatrix
  .in(file("core"))
  .settings(stdSettings)
  .settings(
    name := "scalapb-validate-core",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb.common-protos" %% "pgv-proto-scalapb_0.10" % (pgvVersion + "-0"),
      "com.thesamet.scalapb.common-protos" %% "pgv-proto-scalapb_0.10" % (pgvVersion + "-0") % "protobuf"
    )
  )
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))

lazy val codeGen = projectMatrix
  .in(file("code-gen"))
  .enablePlugins(BuildInfoPlugin)
  .settings(stdSettings)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "scalapb.validate.compiler",
    name := "scalapb-validate-codegen",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb.common-protos" %% "pgv-proto-scalapb_0.10" % (pgvVersion + "-0")
    )
  )
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))

lazy val codeGenJVM212 = codeGen.jvm(Scala212)

lazy val protocGenScalaPbValidate =
  protocGenProject("protoc-gen-scalapb-validate", codeGenJVM212)
    .settings(
      Compile / mainClass := Some("scalapb.validate.compiler.CodeGenerator")
    )

lazy val e2e = projectMatrix
  .in(file("e2e"))
  .dependsOn(core)
  .enablePlugins(LocalCodeGenPlugin)
  .settings(stdSettings)
  .settings(
    skip in publish := true,
    crossScalaVersions := Seq(Scala212, Scala213),
    codeGenClasspath := (codeGenJVM212 / Compile / fullClasspath).value,
    libraryDependencies ++= Seq(
      "io.undertow" % "undertow-core" % "2.1.3.Final",
      "io.envoyproxy.protoc-gen-validate" % "pgv-java-stub" % pgvVersion % "protobuf",
      "org.scalameta" %% "munit" % "0.7.9" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value / "scalapb",
      genModule("scalapb.validate.compiler.CodeGenerator$") ->
        (sourceManaged in Compile).value / "scalapb"
    )
  )
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))
