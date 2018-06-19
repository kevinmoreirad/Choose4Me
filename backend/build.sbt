name := "Choose4MeBackEnd"
 
version := "1.0" 
      
lazy val `choose4mebackend` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.1"
)
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24" // Connecteur MySQL
libraryDependencies += "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5" // JWT for authentication
unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies += filters

      