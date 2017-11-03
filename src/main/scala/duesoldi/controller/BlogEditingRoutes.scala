package duesoldi.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.markdown.MarkdownParser
import duesoldi.model.BlogEntry
import duesoldi.storage.BlogStore
import duesoldi.validation.{ValidBlogContent, ValidIdentifier}

import scala.concurrent.ExecutionContext

object BlogEditingRoutes
{
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def blogEditingRoutes(implicit executionContext: ExecutionContext,
                        inject: RequestDependencyInjector): Route =
    path("admin" / "blog" / Remaining) { remaining =>
      inject.dependencies[BlogStore, Credentials, MarkdownParser] into { case (blogStore, credentials, markdownParser) =>
        adminsOnly(credentials) {
          put {
            entity(as[String]) { content =>
              complete {
                (for {
                  _ <- ValidIdentifier(remaining).failWith({ s"Identifier invalid: '$remaining'" })
                  document = markdownParser.markdown(content)
                  _ <- ValidBlogContent(document).failOnSomeWith(reason => s"Content invalid: $reason")
                  result <- blogStore.store(BlogEntry(remaining, document)).failWith(_ => "Failed to store entry" )
                } yield {
                  result
                }).value.map {
                  case Right(_) => HttpResponse(201)
                  case Left(failureReason) => HttpResponse(400, entity = failureReason)
                }
              }
            }
          } ~ delete {
            complete {
              for {
                result <- blogStore.delete(remaining)
              } yield {
                HttpResponse(204)
              }
            }
          } ~ get {
            complete {
              for {
                result <- blogStore.entry(remaining)
              } yield {
                result match {
                  case Some(entry) => HttpResponse(200, entity = entry.content.raw)
                  case _ => HttpResponse(404)
                }
              }
            }
          }
        }
      }
    }
}
