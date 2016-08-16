package io.bookwitz.controllers

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.{bookWordsList, booksList, dictionaryWordsList}
import io.bookwitz.web.models.{Book, BookWord}
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

  def bookUpload = SecuredAction.async(parse.maxLength(1024 * 1024, parse.multipartFormData)) { request => {
    request.body match {
      case Left(MaxSizeExceeded(length)) => {
        logger.error("Progress response error. File is too large " + length )
        Future(BadRequest("Your file is too large, we accept just " + length + " bytes!"))
      }
      case Right(multipartForm) => {
        val (progressEnumerator, progressChannel) = Concurrent.broadcast[JsValue]
        val file = multipartForm.files.head.ref.file
        val httpRequest = HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/bookUpload",
          entity = FormData("content" -> scala.io.Source.fromFile(file).mkString, "title" -> file.getName).toEntity)
        val response = Http().singleRequest(httpRequest)
        response onFailure {
          case result =>
            logger.error("Progress response error ", result)
            InternalServerError("failure")
        }
        response.flatMap(
          response => {
            val entity = response.entity.toStrict(5 seconds)
            entity onFailure {
              case result =>
                logger.error("Progress response error ", result)
                InternalServerError("failure")
            }

            entity.flatMap(
              entity => {
                if (response.status.isSuccess()) {
                  val uuid = entity.data.decodeString("UTF-8")
                  logger.debug("Upload response " + uuid)
                  Future(Ok(uuid))
                } else {
                  logger.error("Progress response error " + response.status.reason())
                  Future(InternalServerError("failure"))
                }
              }
            )
          }
        )

      }
    }
  }
  }

  def bookProcessProgress(uuid: String) = SecuredAction.async { request => {

    val httpRequest = HttpRequest(method = HttpMethods.GET,
      uri = Uri("http://localhost:8080/bookUpload").withQuery(Uri.Query("uuid" -> uuid)))
    val response = Http().singleRequest(httpRequest)
    response onFailure {
      case result =>
        logger.error("Progress response error", result)
        InternalServerError("failure")
    }
    response.flatMap(
      response => {
        val entity = response.entity.toStrict(5 seconds)
        entity onFailure {
          case result =>
            logger.error("Progress response error", result)
            InternalServerError("failure")
        }

        entity.flatMap(
          entity => {
            val text = entity.data.decodeString("UTF-8")
            logger.debug("Progress response " + text)
            Future(Ok(Json.parse(text)))
          }
        )
      }
    )
  }
  }
}