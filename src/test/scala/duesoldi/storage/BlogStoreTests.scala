package duesoldi.storage

import duesoldi.markdown.MarkdownDocument.{Heading, Text}
import duesoldi.markdown.{MarkdownDocument, MarkdownParser}
import duesoldi.storage.BlogStore.InvalidContent
import org.scalatest.Matchers._
import org.scalatest.{AsyncWordSpec, EitherValues}

import scala.concurrent.Future

class BlogStoreTests extends AsyncWordSpec with EitherValues {

  "A blog store" must {

    "return a not found failure when the markdown source does not have the document" in {
      val source = new MarkdownSource {
        override def document(id: String) = Future successful None
        override def documents: Future[Seq[(String, String)]] = Future successful Seq.empty
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("non-existent") map { _ shouldBe Left(BlogStore.NotFound) }
    }

    "return a blog entry with the title from the parsed document" in {
      val source = new MarkdownSource {
        override def document(id: String) = Future successful Some("# A document!")
        override def documents: Future[Seq[(String, String)]] = Future successful Seq.empty
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("exists") map { _.right.value.title shouldBe "A document!" }
    }

    "return a blog entry with the parsed document as the content" in {
      val source = new MarkdownSource {
        override def document(id: String) = Future successful Some("# A document!")
        override def documents: Future[Seq[(String, String)]] = Future successful Seq.empty
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("exists") map { _.right.value.content shouldBe MarkdownDocument(Seq(Heading(Seq(Text("A document!")), 1))) }
    }

    "returns an invalid content failure if the markdown does not have a title" in {
      val source = new MarkdownSource {
        override def document(id: String) = Future successful Some("No title! Wheeeee!")
        override def documents: Future[Seq[(String, String)]] = Future successful Seq.empty
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entry("titleless") map { _.left.value shouldBe InvalidContent }
    }

    "return an empty sequence if the source has no documents" in {
      val source = new MarkdownSource {
        override def document(id: String) = Future successful None
        override def documents: Future[Seq[(String, String)]] = Future successful Seq.empty
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entries map { _ shouldBe empty }
    }

    "returns a sequence containing the document if the source has one document" in {
      val source = new MarkdownSource {
        override def document(id: String): Future[Option[String]] = ???
        override def documents: Future[Seq[(String, String)]] = Future successful Seq("id" -> "# Content")
      }
      val store = new BlogStore(source, new MarkdownParser)
      store.entries map { _.head should have('title ("Content"),
                                             'id    ("id"))}
    }

  }


}
