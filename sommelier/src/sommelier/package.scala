package object sommelier
{
  type Context = handling.Context
  type Request = ratatoskr.Request
  type Response = ratatoskr.Response
  type Server = serving.Server
  type Controller = routing.Controller
}

// todo more tests
// todo restructure - DSL on sommelier package object, implementation stuff in packages
// todo injection = hornetfish
// todo reversible / named routes:
/* is this possible?:

  "blog entry" as GET("/blog/:id") ->- { _ => ... }

  router("blog entry").reverse("id" -> "foo).path // reverse results in a request?

 */
// todo abstract completely from implementation
// rejection should always be a response...? or extend Response?