import scalapb.GeneratorOption

name := "validate-example"

scalaVersion := "2.13.2"

ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

scalacOptions in ThisBuild ++= Seq("-Xfatal-warnings", "-Xlint")

val options: Set[GeneratorOption] = Set(GeneratorOption.FlatPackage)

PB.targets in Compile := Seq(
  scalapb.gen(options) -> (sourceManaged in Compile).value / "scalapb",
  scalapb.validate.gen(options) -> (sourceManaged in Compile).value / "scalapb"
)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf",
)
