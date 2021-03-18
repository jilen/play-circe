resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scoverage"     % "sbt-scoverage"       % "1.6.1")
addSbtPlugin("com.codacy"        % "sbt-codacy-coverage" % "3.0.3")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.13")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"             % "2.0.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"        % "2.4.0")
