package duesoldi.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import duesoldi.config.Config.Credentials
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector
import duesoldi.markdown.MarkdownParser.ParseMarkdown
import duesoldi.model.BlogEntry
import duesoldi.storage.blog.{DeleteBlogEntry, GetBlogEntry, PutBlogEntry}
import duesoldi.validation.{ValidBlogContent, ValidIdentifier}

import scala.concurrent.ExecutionContext

object BlogEditingRoutes
{
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def blogEditingRoutes(implicit executionContext: ExecutionContext,
                        inject: RequestDependencyInjector): Route =
    path("admin" / "blog" / Remaining) { id =>
      inject.dependencies[Credentials, ParseMarkdown] into { case (credentials, parseMarkdown) =>
        adminsOnly(credentials) {
          put {
            entity(as[String]) { content =>
              inject.dependency[PutBlogEntry] into { putBlogEntry =>
                complete {
                  (for {
                    _ <- ValidIdentifier(id).failWith({ HttpResponse(400, entity = s"Identifier invalid: '$id'") })
                    document = parseMarkdown(content)
                    _ <- ValidBlogContent(document).failOnSomeWith(reason => HttpResponse(400, entity = s"Content invalid: $reason"))
                    result <- putBlogEntry(BlogEntry(id, document)).failWith(_ => HttpResponse(500, entity = "Failed to store entry"))
                  } yield {
                    HttpResponse(201)
                  }).value
                }
              }
            }
          } ~ delete {
            inject.dependency[DeleteBlogEntry] into { deleteEntry =>
              complete {
                for {
                  result <- deleteEntry(id)
                } yield {
                  HttpResponse(204)
                }
              }
            }
          } ~ get {
            inject.dependency[GetBlogEntry] into { getEntry =>
              complete {
                (for {
                  entry <- getEntry(id).failWith({ HttpResponse(404) })
                } yield {
                  HttpResponse(200, entity = entry.content.raw)
                }).value
              }
            }
          }
        }
      }
    }
}
