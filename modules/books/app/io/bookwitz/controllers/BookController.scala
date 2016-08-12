package io.bookwitz.controllers


import java.io.File

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.bookwitz.models.BooksTableQueries.{bookWordsList, booksList, dictionaryWordsList}
import io.bookwitz.models.{Book, BookWord}
import io.bookwitz.users.models.BasicUser
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.MaxSizeExceeded
import securesocial.core.{RuntimeEnvironment, SecureSocial}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.slick.driver.JdbcDriver.simple._


object BookController {
  val system = ActorSystem("process")
}


class BookController(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {

  val logger = Logger(getClass)
  var progressChannel: Concurrent.Channel[JsValue] = null
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  def books = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[Book]
      Ok(Json.stringify(Json.toJson(booksList.list)))
    }
  }
  }


  implicit val writer = new Writes[(Long, String, String, Long)] {
    def writes(t: (Long, String, String, Long)): JsValue = {
      Json.obj("bookId" -> t._1, "word" -> t._2, "tag" -> t._3, "freq" -> t._4)
    }
  }

  def bookWords(bookId: Long) = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[BookWord]


      val innerJoin = for {
        (b, d) <- bookWordsList join dictionaryWordsList on (_.word === _.id)
      } yield (b.bookId, d.word, b.tagColumn, b.freq)
      Ok(Json.stringify(Json.toJson(innerJoin.filter(_._1 === bookId).list)))
    }
  }
  }

  def bookUpload = SecuredAction(parse.maxLength(5 * 1024 * 1024, parse.multipartFormData)) { request => {
    request.body match {
      case Left(MaxSizeExceeded(length)) => {
        Logger.error("MaxSizeExceeded")
        BadRequest("Your file is too large, we accept just " + length + " bytes!")
      }
      case Right(multipartForm) => {
        val (progressEnumerator, progressChannel) = Concurrent.broadcast[JsValue]
        val uuid = java.util.UUID.randomUUID().toString()
        val file = multipartForm.files.head.ref.file
        val newFile = new File(file.getParentFile, uuid)
        file.renameTo(newFile)
        val title = multipartForm.files.head.filename
        val httpRequest = HttpRequest(method = HttpMethods.POST, uri = "https://dictwitz.herokuapp.com/bookUpload")
        Http().singleRequest(httpRequest) flatMap {
          case response: HttpResponse =>
            response.entity.toStrict(5 seconds).map(_.data.decodeString("UTF-8")).map(result =>
              Ok(Json.parse(result))
            )
        }
        Ok(uuid);
      }
    }
  }
  }

  def bookProcessProgress(uuid: String) = SecuredAction.async { request => {
    val actorPath: ActorPath = BookController.system / uuid
    val actorSelection = BookController.system.actorSelection(actorPath)
    val future = actorSelection.resolveOne(10 second)

    future onFailure {
      case actorRef => {
        InternalServerError("failure")
      }
    }

    future.flatMap(
      response => {
        implicit val timeout = Timeout(5 seconds)
        val askFuture: Future[String] = ask(response, "progress").mapTo[String]
        askFuture.onFailure {
          case result =>
            InternalServerError("failure");
        }
        askFuture.map(
          message =>
            Ok(message.toString)
        )
      }
    )
  }
  }
}