package duesoldi.storage

import java.time.ZonedDateTime

import duesoldi.markdown.MarkdownDocument.{Heading, Text}
import duesoldi.markdown.{MarkdownDocument, MarkdownParser}
import duesoldi.storage.BlogStore.InvalidContent
import org.scalatest.Matchers._
import org.scalatest.{AsyncWordSpec, EitherValues}

import scala.collection.mutable
import scala.concurrent.Future

class BlogStoreTests extends AsyncWordSpec with EitherValues {

  "A blog store" must {

    "return a not found failure when the markdown source does not have the document" in {
//      val source = new MarkdownSource {
//        override def document(id: String) = Future successful None
//        override def documents = Future successful Seq.empty
//      }
      val source = markdownSource
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("non-existent") map { _ shouldBe Left(BlogStore.NotFound) }
    }

    "return a blog entry with the title from the parsed document" in {
//      val source = new MarkdownSource {
//        override def document(id: String) = Future successful Some(MarkdownContainer(content = "# A document!"))
//        override def documents = Future successful Seq.empty
//      }
      val source = markdownSource.withDocument("exists", content = "# A document!")
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("exists") map { _.right.value.title shouldBe "A document!" }
    }

    "return a blog entry with the parsed document as the content" in {
//      val source = new MarkdownSource {
//        override def document(id: String) = Future successful Some(MarkdownContainer(content = "# A document!"))
//        override def documents = Future successful Seq.empty
//      }
      val source = markdownSource.withDocument("exists", content = "# A document!")
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("exists") map { _.right.value.content shouldBe MarkdownDocument(Seq(Heading(Seq(Text("A document!")), 1))) }
    }

    "returns an invalid content failure if the markdown does not have a title" in {
//      val source = new MarkdownSource {
//        override def document(id: String) = Future successful Some(MarkdownContainer(content = "No title! Wheeeee!"))
//        override def documents = Future successful Seq.empty
//      }
      val source = markdownSource.withDocument(id = "titleless", content = "No title! Wheeeee!")
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("titleless") map { _.left.value shouldBe InvalidContent }
    }

    "return an empty sequence if the source has no documents" in {
//      val source = new MarkdownSource {
//        override def document(id: String) = Future successful None
//        override def documents = Future successful Seq.empty
//      }
      val source = markdownSource
      val store = new BlogStore(source, new MarkdownParser)
      store.entries map { _ shouldBe empty }
    }

    "returns a sequence containing the document if the source has one document" in {
//      val source = new MarkdownSource {
//        override def document(id: String): Future[Option[MarkdownContainer]] = ???
//        override def documents: Future[Seq[(String, MarkdownContainer)]] = Future successful Seq("id" -> MarkdownContainer(content = "# Content"))
//      }
      val source = markdownSource.withDocument(id = "id", content = "# Content")
      val store = new BlogStore(source, new MarkdownParser)
      store.entries map { _.head should have('title ("Content"),
                                             'id    ("id"))}
    }

    "includes the last modified date of the markdown document in the blog entry" in {
//      val container = MarkdownContainer(content = "# Content!", lastModified = ZonedDateTime.now())
//      val source = new MarkdownSource {
//        override def document(id: String): Future[Option[MarkdownContainer]] = Future successful Some(container)
//        override def documents: Future[Seq[(String, MarkdownContainer)]] = ???
//      }
//
      val lastModified = ZonedDateTime.now()
      val source = markdownSource.withDocument(id = "entry", content = "# Content!", lastModified = lastModified)
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("entry") map { _.right.value should have('lastModified (lastModified))}
    }

  }

  private def markdownSource = MarkdownSourceBuilder()

  private case class MarkdownSourceBuilder(allDocuments: collection.mutable.Map[String, MarkdownContainer] = mutable.HashMap()) extends MarkdownSource {

    def withDocument(id: String, content: String = "# Content!", lastModified: ZonedDateTime = ZonedDateTime.now()): MarkdownSourceBuilder = {
      copy(allDocuments + (id -> MarkdownContainer(lastModified, content)))
    }

    override def document(id: String): Future[Option[MarkdownContainer]] = Future successful allDocuments.get(id)
    override def documents: Future[Seq[(String, MarkdownContainer)]] = Future successful allDocuments.toList
    override def store(id: String, markdown: MarkdownContainer): Future[Unit] = Future successful allDocuments.put(id, markdown)
  }


}
