package io.bookwitz.controllers

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import io.bookwitz.users.models.BasicUser
import io.bookwitz.web.models.BooksTableQueries.{booksList, userWordsList}
import io.bookwitz.web.models.{Book, UserWord}
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{MaxSizeExceeded, Result}
import securesocial.core.{RuntimeEnvironment, SecureSocial}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.slick.driver.JdbcDriver.simple._


object BookController {
}


class BookController(override implicit val env: RuntimeEnvironment[BasicUser]) extends SecureSocial[BasicUser] {

  val PARSER_URI: String = "https://dictwitz.herokuapp.com/bookUpload"

  val logger = Logger(getClass)
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

  def userWords() = SecuredAction { request => {
    Database.forDataSource(DB.getDataSource()) withSession { implicit session =>
      implicit val writes = Json.writes[UserWord]
      Ok(userWordsList.filter(_.userId == request.user.main.userId).list)
    }
  }
  }


  //TODO
  def contentUpload = SecuredAction.async(parse.json(maxLength = 1024 * 1024)) { request => {
    forwardUpload(request.body.\("content").as[String])
  }
  }

  def bookUpload = SecuredAction.async(parse.maxLength(1024 * 1024, parse.multipartFormData)) { request => {
    request.body match {
      case Left(MaxSizeExceeded(length)) => {
        logger.error("Progress response error. File is too large " + length)
        Future(BadRequest("Your file is too large, we accept just " + length + " bytes!"))
      }
      case Right(multipartForm) => {
        val (progressEnumerator, progressChannel) = Concurrent.broadcast[JsValue]
        val file = multipartForm.files.head.ref.file
        forwardUpload(scala.io.Source.fromFile(file).mkString)
      }
    }
  }
  }

  def bookProcessProgress(uuid: String) = SecuredAction.async { request => {

    val httpRequest = HttpRequest(method = HttpMethods.GET,
      uri = Uri(PARSER_URI).withQuery(Uri.Query("uuid" -> uuid)))
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

  def forwardUpload(content: String): Future[Result] = {
    val httpRequest = HttpRequest(method = HttpMethods.POST, uri = PARSER_URI,
      entity = FormData("content" -> content).toEntity)
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