//
//  Author: Nho Luong
//  Date: 2016-06-06 22:51:45 +0100 (Mon, 06 Jun 2016)
//
//  vim:ts=4:sts=4:sw=4:et:filetype=java
//
//  https://github.com/nholuongut/lib-java
//
//  License: see accompanying Nho Luong LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help improve or steer this or other code I publish
//
//  https://www.linkedin.com/in/nholuong
//

// TODO: metadata from pom, description, URLs, different name to jar artifact id

// This comes out with spaces and capitalization with sbt assembly
organization := "com.linkedin.nholuongut"
name := "nholuongut-utils"

version := "1.17.6"

// This must be aligned with nagios-plugin-kafka etc
scalaVersion := "2.12.8"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// add assembly artifact to publish
artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.withClassifier(Some("assembly"))
}
addArtifact(artifact in (Compile, assembly), assembly)

// not updated for sbt 1.x
//enablePlugins(VersionEyePlugin)

//existingProjectId in versioneye := "57616cdb0a82b20053182c74"
//baseUrl in versioneye := "https://www.versioneye.com"
//apiPath in versioneye := "/api/v2"
//publishCrossVersion in versioneye := true

libraryDependencies ++= Seq (
    "commons-cli" % "commons-cli" % "1.4",
    "commons-lang" % "commons-lang" % "2.6",
    "jline" % "jline" % "2.14.5",
    "junit" % "junit" % "4.12",
    "log4j" % "log4j" % "1.2.17",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
    //"net.sf.jopt-simple" % "jopt-simple" % "4.9"
)
