package duesoldi.blog.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import duesoldi.blog.storage.{DeleteBlogEntry, GetAllBlogEntries, GetBlogEntry, PutBlogEntry}
import duesoldi.blog.validation.{ValidateContent, ValidateIdentifier}
import duesoldi.blog.{EntriesToYaml, EntryFromYaml, EntryToYaml}
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
    pathPrefix("admin" / "blog") {
      inject.dependencies[Credentials, ValidateIdentifier] into { case (credentials, validateIdentifier) =>
        adminsOnly(credentials) {
          path(Segment) { id =>
            put {
              entity(as[String]) { content =>
                inject.dependencies[PutBlogEntry, ValidateContent, EntryFromYaml, GetBlogEntry] into { case (putEntry, validateContent, parse, getEntry) =>
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
                  (for {
                    _ <- validateIdentifier(id).failOnSomeWith(_ => {
                      HttpResponse(400, entity = "Invalid ID")
                    })
                    _ <- deleteEntry(id).failWith(_ => {
                      HttpResponse(500, entity = "Failed to delete blog entry")
                    })
                  } yield {
                    HttpResponse(204)
                  }).value
                }
              }
            } ~ get {
              inject.dependencies[GetBlogEntry, EntryToYaml] into { case (getEntry, format) =>
                complete {
                  (for {
                    _ <- validateIdentifier(id).failOnSomeWith(_ => {
                      HttpResponse(400, entity = "Invalid ID")
                    })
                    entry <- getEntry(id).failWith({
                      HttpResponse(404)
                    })
                    yaml = format(entry)
                  } yield {
                    HttpResponse(200, entity = yaml)
                  }).value
                }
              }
            }
          } ~ pathEndOrSingleSlash {
            get {
              inject.dependencies[GetAllBlogEntries, EntriesToYaml] into { case (getEntries, format) =>
                complete {
                  for {
                    entries <- getEntries()
                  } yield {
                    if (entries.isEmpty) {
                      HttpResponse(204)
                    } else {
                      HttpResponse(200, entity = format(entries))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
}
