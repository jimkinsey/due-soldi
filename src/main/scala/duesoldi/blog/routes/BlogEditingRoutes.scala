package duesoldi.blog.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import duesoldi.blog.storage.{DeleteBlogEntry, GetBlogEntry, PutBlogEntry}
import duesoldi.blog.validation.{ValidateContent, ValidateIdentifier}
import duesoldi.blog.{EntryFromYaml, EntryToYaml}
import duesoldi.config.Config.Credentials
import duesoldi.controller.AdminAuthentication.adminsOnly
import duesoldi.dependencies.DueSoldiDependencies._
import duesoldi.dependencies.RequestDependencyInjection.RequestDependencyInjector

import scala.concurrent.ExecutionContext

object BlogEditingRoutes
{
  import cats.instances.all._
  import duesoldi.transformers.TransformerOps._

  def blogEditingRoutes(implicit executionContext: ExecutionContext,
                        inject: RequestDependencyInjector): Route =
    path("admin" / "blog" / Remaining) { id =>
      inject.dependency[Credentials] into { credentials =>
        adminsOnly(credentials) {
          put {
            entity(as[String]) { content =>
              inject.dependencies[PutBlogEntry, ValidateIdentifier, ValidateContent, EntryFromYaml, GetBlogEntry] into { case (putEntry, validateIdentifier, validateContent, parse, getEntry) =>
                complete {
                  (for {
                    _ <- validateIdentifier(id).failOnSomeWith(reason => HttpResponse(400, entity = s"Identifier invalid: $reason"))
                    _ <- getEntry(id).failOnSomeWith(_ => HttpResponse(409, entity = s"Entry with ID '$id' already exists"))
                    entry <- parse(content).failWith(f => HttpResponse(400, entity = s"Failed to parse this content [$f] [$content]"))
                    _ <- validateContent(entry.content).failOnSomeWith(reason => HttpResponse(400, entity = s"Content invalid: $reason"))
                    result <- putEntry(entry).failWith(_ => HttpResponse(500, entity = "Failed to store entry"))
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
            inject.dependencies[GetBlogEntry, EntryToYaml] into { case (getEntry, format) =>
              complete {
                (for {
                  entry <- getEntry(id).failWith({ HttpResponse(404) })
                  yaml = format(entry)
                } yield {
                  HttpResponse(200, entity = yaml)
                }).value
              }
            }
          }
        }
      }
    }
}
