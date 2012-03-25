// import must be at top of build.sbt, or SBT will complain
import com.mojolly.scalate.ScalatePlugin._

organization := "de.measite"

name         := "virtadmin"

version      := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked"
)

// Remove this when Netty 4 is released (this must be put before Xitrum below)
libraryDependencies += "io.netty" % "netty" % "4.0.0.Alpha1-SNAPSHOT" from "http://cloud.github.com/downloads/ngocdaothanh/xitrum/netty-4.0.0.Alpha1-SNAPSHOT.jar"

// Xitrum uses Jerkson: https://github.com/codahale/jerkson

resolvers += "repo.codahale.com" at "http://repo.codahale.com"

// An implementation of SLF4J must be provided for Xitrum

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.0"

libraryDependencies += "tv.cntt" %% "xitrum" % "1.8.6"

libraryDependencies += "jmdns" % "jmdns" % "1.0"

libraryDependencies += "org.libvirt" % "libvirt" % "0.4.7"

libraryDependencies += "net.java.dev.jna" % "jna" % "3.0.9"

libraryDependencies += "net.schmizz" % "sshj" % "0.7.0"

libraryDependencies += "com.jcraft" % "jzlib" % "1.0.7"

libraryDependencies += "org.apache.sshd" % "sshd-core" % "0.5.0"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"

libraryDependencies += "org.apache.mina" % "mina-core" % "2.0.4"

libraryDependencies += "com.jcraft" % "jsch" % "0.1.46"

// xgettext i18n translation key string extractor is a compiler plugin ---------

autoCompilerPlugins := true

addCompilerPlugin("tv.cntt" %% "xitrum-xgettext" % "1.1")

// xitrum.imperatively uses Scala continuation, also a compiler plugin ---------

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.1")

scalacOptions += "-P:continuations:enable"

// Precompile Scalate ----------------------------------------------------------

seq(scalateSettings:_*)

scalateTemplateDirectory in Compile <<= (baseDirectory) { _ / "src/main/view" }

scalateBindings += Binding("helper", "xitrum.Controller", true)

// Put config directory in classpath for easier development --------------------

// For "sbt console"
unmanagedClasspath in Compile <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }

// For "sbt run"
unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }
