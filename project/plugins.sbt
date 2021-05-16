resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scoverage"     % "sbt-scoverage"       % "1.8.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"         % "1.0.13")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"        % "3.9.7")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"             % "2.1.1")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"        % "2.4.2")
