resolvers += Resolver.bintrayIvyRepo("hmrc", "sbt-plugin-releases")
resolvers += Resolver.bintrayRepo("hmrc", "releases")

addSbtPlugin("uk.gov.hmrc"       %  "sbt-auto-build"        % "2.10.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-artifactory"       % "1.6.0")
addSbtPlugin("uk.gov.hmrc"       %  "sbt-git-versioning"    % "2.1.0")
addSbtPlugin("org.scoverage"     %  "sbt-scoverage"         % "1.6.1")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.typesafe.play" %  "sbt-plugin"            % "2.6.23")
