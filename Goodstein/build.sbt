import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "scalakittens"
ThisBuild / organizationName := "Vlad Patryshev"

ThisBuild / libraryDependencies += ("org.scala-lang.modules" %% "scala-parser-combinators" % "2.11.0-M4")
ThisBuild / libraryDependencies += ("org.specs2" %% "specs2-scalacheck" % "4.13.2" % Test) withSources()


lazy val root = (project in file("."))
  .settings(
    name := "Goodstein",
    libraryDependencies += scalaTest % Test
  )
