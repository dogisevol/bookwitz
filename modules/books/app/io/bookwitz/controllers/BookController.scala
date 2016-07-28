package io.bookwitz.controllers


import java.io.File

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import io.bookwitz.actors.BookProgressActor
import io.bookwitz.models.{Book, BookWord}
import io.bookwitz.models.BooksTableQueries.{bookWordsList, booksList}
import io.bookwitz.service.WordnikService
import io.bookwitz.users.models.BasicUser
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSClient
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

  def books = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[Book]
      Ok(Json.stringify(Json.toJson(booksList.list)))
    }
  }
  }

  implicit val writer = new Writes[(String, String)] {
    def writes(t: (String, String)): JsValue = {
      Json.obj()
    }
  }

  def bookWords(bookId: Long) = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[BookWord]
      Ok(Json.stringify(Json.toJson(bookWordsList.filter(_.bookId === bookId).list)))
    }
  }
  }

  def bookUpload = SecuredAction { request => {
    val (progressEnumerator, progressChannel) = Concurrent.broadcast[JsValue]
    val uuid = java.util.UUID.randomUUID().toString()
    val file = request.body.asMultipartFormData.get.files.head.ref.file
    val newFile = new File(file.getParentFile, uuid)
    file.renameTo(newFile)
    val title = request.body.asMultipartFormData.get.files.head.filename
    BookController.system.actorOf(Props(new BookProgressActor(newFile, request.user, title)), uuid) ! "start"
    Ok(uuid);
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