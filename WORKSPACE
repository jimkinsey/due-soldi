rules_scala_version="a8469d3a359b566b52ffae00b91c372d987f42e8" # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip"%rules_scala_version,
    type = "zip",
    strip_prefix= "rules_scala-%s" % rules_scala_version
)

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories()

maven_server(
    name = "sonatype_oss_snapshots",
    url = "https://oss.sonatype.org/content/repositories/snapshots",
)

maven_server(
    name = "sonatype_oss_releases",
    url = "https://oss.sonatype.org/content/repositories/releases",
)

maven_jar(
  name = "postgresql",
  artifact = "org.postgresql:postgresql:9.4.1212",
)

maven_jar(
  name = "flexmark",
  artifact = "com.vladsch.flexmark:flexmark:0.27.0",
)

maven_jar(
  name = "flexmark_util",
  artifact = "com.vladsch.flexmark:flexmark-util:0.27.0",
)

maven_jar(
  name = "bhuj",
  artifact = "com.github.jimkinsey:bhuj_2.12:0.2-SNAPSHOT",
  server = "sonatype_oss_snapshots",
)

maven_jar(
  name = "utest",
  artifact = "com.lihaoyi:utest_2.12:0.5.4",
)

maven_jar(
  name = "jsoup",
  artifact = "org.jsoup:jsoup:1.10.3",
)

maven_jar(
  name = "h2",
  artifact = "com.h2database:h2:1.4.193",
)

maven_jar(
  name = "dispatch",
  artifact = "net.databinder.dispatch:dispatch-core_2.11:0.13.2",
  server = "sonatype_oss_releases",
)

maven_jar(
  name = "netty_handler",
  artifact = "io.netty:netty-handler:4.0.51.Final",
  server = "sonatype_oss_releases",
)

maven_jar(
  name = "netty_codec",
  artifact = "io.netty:netty-codec:4.0.51.Final",
  server = "sonatype_oss_releases",
)

maven_jar(
  name = "netty_codec_http",
  artifact = "io.netty:netty-codec-http:4.0.51.Final",
  server = "sonatype_oss_releases",
)

maven_jar(
  name = "async_http_client",
  artifact = "org.asynchttpclient:async-http-client:2.0.36",
)
