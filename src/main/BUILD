load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library", "scala_binary", "scala_test")

scala_binary(
    name = "Blog",
    visibility = ["//visibility:public"],
    srcs = glob(["scala/**/*.scala"]),
    main_class = "duesoldi.App",
    deps = [
        "@postgresql//jar",
        "@flexmark//jar",
        "@flexmark_util//jar",
        "@bhuj//jar",
    ],
)
