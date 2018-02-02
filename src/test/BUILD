load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library", "scala_binary", "scala_test")

java_library(
    name = "ScriptRunner",
    srcs = glob(["scala/**/*.java"]),
)

scala_binary(
    name = "Test",
    srcs = glob(["scala/**/*.scala"]),
    main_class = "test.RunTests",
    deps = [
        "//src/main:Blog",
        "ScriptRunner",
        "@h2//jar",
        "@async_http_client//jar",
        "@netty_handler//jar",
        "@netty_codec//jar",
        "@netty_codec_http//jar",
        "@dispatch//jar",
        "@jsoup//jar",
        "@utest//jar:file",
        "@utest//jar",
    ],
)
