package object sommelier
{
  type Context = handling.Context
  type Request = messaging.Request
  type Response = messaging.Response
  type Server = serving.Server
  type Controller = routing.Controller
}

// todo more tests
// todo restructure - DSL on sommelier package object, implementation stuff in packages
// todo stress testing
// todo events = dearboy, injection = hornetfish
// todo why 405 instead of 404 on so many paths?
// todo reversible / named routes:
/* is this possible?:

  "blog entry" as GET("/blog/:id") ->- { _ => ... }

  router("blog entry").reverse("id" -> "foo).path // reverse results in a request?

 */